package spigey.bot.system;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static spigey.bot.DiscordBot.prefix;
import static spigey.bot.system.sys.errInfo;
import static spigey.bot.system.util.*;
import static spigey.bot.system.util.msg;

public class CommandHandler {
    private final Map<String, Command> commands = new HashMap<>();
    private final Map<String, String> aliasToCommandMap = new HashMap<>();
    private final Map<String, CooldownManager> cooldownManagers = new HashMap<>();
    private File[] files;

    public CommandHandler() {
        loadCommands();
    }

    private void loadCommands() {
        try {
            debug("Registering commands", false);
            File dir = new File(getClass().getClassLoader().getResource("spigey/bot/Commands").getFile());
            loadDir(dir);
            files = fileRecur(dir);
        } catch (Exception e) {
            sys.errInfo(e);
        }
    }

    public void reloadCommands() {
        commands.clear();
        aliasToCommandMap.clear();
        cooldownManagers.clear();
        loadCommands();
    }

    public CooldownManager getCooldownManager(String commandName) {
        return cooldownManagers.get(commandName);
    }

    public void doTheActualShit(MessageReceivedEvent event) throws IOException, ParseException {
        String[] split = event.getMessage().getContentRaw().split(" ");
        if (!split[0].startsWith(prefix)) return;

        String commandName = split[0].substring(1).toLowerCase();
        String resolvedCommandName = aliasToCommandMap.getOrDefault(commandName, commandName);
        Command command = commands.get(resolvedCommandName);

        if (command != null) {
            db.add("properties", "cmds", 1);
            String[] args = new String[split.length - 1];
            System.arraycopy(split, 1, args, 0, args.length);
            try {
                if (command.getClass().isAnnotationPresent(CommandInfo.class)) {
                    CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);

                    // Check if user is limited
                    if (info.limitIds().length > 0 && !Arrays.asList(info.limitIds()).contains(event.getAuthor().getId())) {
                        event.getChannel().sendMessage(info.limitMsg()).queue();
                        return;
                    }

                    // Check cooldown
                    CooldownManager cooldownManager = cooldownManagers.get(resolvedCommandName);
                    if (cooldownManager != null && cooldownManager.isActive(event.getAuthor())) {
                        String remainingCooldown = cooldownManager.parse(event.getAuthor());
                        event.getChannel().sendMessage("You have to wait " + remainingCooldown + " before using this command again.").queue();
                        return;
                    }

                    command.execute(event, event.getMessage().getContentRaw().split(" "));

                    // Update cooldown after successful execution
                    if (cooldownManager != null) {
                        cooldownManager.update(event.getAuthor());
                    }
                }
            } catch (Exception e) {
                errInfo(e);
            }
        }
    }

    public void onSlashCommand(SlashCommandInteractionEvent event) throws IOException, ParseException {
        db.add("properties", "cmds", 1);
        Command command = null;
        try {
            for (File file : files) {
                if (file.getName().endsWith(".class")) {
                    Class<?> cls = Class.forName("spigey.bot." + file.getPath()
                            .replace(File.separator, ".")  // Replace file separators with dots
                            .substring(file.getPath().indexOf("spigey\\bot") + "spigey.bot".length() + 1) // Get class name after "spigey.bot."
                            .replace(".class", ""));
                    if (cls.isAnnotationPresent(CommandInfo.class)) {
                        CommandInfo info = cls.getAnnotation(CommandInfo.class);
                        if (info.slashCommand().equalsIgnoreCase(event.getName())) {
                            command = (Command) cls.getDeclaredConstructor().newInstance();
                            break;
                        }
                    }
                }
            }
        } catch(Exception L){
            errInfo(L);
        }
        if (command == null){
            event.reply("Due to my bad command handler, the command was not found. Please report this bug.").setEphemeral(true).queue();
            sys.error("Slash Command not found: /" + event.getName().toLowerCase());
            return;
        }
        try {
            if (command.getClass().isAnnotationPresent(CommandInfo.class)) {
                CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);
                if (info.limitIds().length > 0 && !Arrays.asList(info.limitIds()).contains(event.getUser().getId())) {event.getChannel().sendMessage(info.limitMsg()).queue(); return;}
                CooldownManager cooldownManager = cooldownManagers.get(command.getClass().getSimpleName().toLowerCase().replace("command", ""));
                if (cooldownManager != null && cooldownManager.isActive(event.getUser())) {
                    String remainingCooldown = cooldownManager.parse(event.getUser());
                    event.reply("You have to wait " + remainingCooldown + " before using this command again.").setEphemeral(true).queue();
                    return;
                }
                boolean success = false;
                if(Objects.equals(db.read(event.getUser().getId(), "banned"), "0")){success = command.slashCommand(event) == 1;}else{
                    StringBuilder cmd = new StringBuilder("/" + event.getName() + " ");
                    List<OptionMapping> options = event.getOptions();
                    for (OptionMapping option : options) {
                        cmd.append(option.getName()).append(":").append(option.getAsString()).append(" ");
                    }
                    sys.warn(event.getUser().getName() + ": " + cmd);
                    event.reply("ok").setEphemeral(true).queue();
                }

                // Update cooldown after successful execution
                if (cooldownManager != null) {
                    if(success) cooldownManager.update(event.getUser());
                }
            }
        } catch (Exception e) {
            StringBuilder err = new StringBuilder(e + "\n   ");
            for(int i = 0; i < e.getStackTrace().length - 1; i++){
                err.append(e.getStackTrace()[i]).append("\n   ");
            }
            err.append(e.getStackTrace()[e.getStackTrace().length - 1]);
            error("A critical has occurred while executing Command:\n" + e + "\nMessage: " + event.getName(), false);
            event.reply("A critical error occurred while executing Command: ```" + (err.toString().length() > 1000 ? err.substring(0, 1000) + "..." : err.toString()) + "```\nThis error has been automatically reported.").setEphemeral(true).queue();
            TextChannel channel = event.getJDA().getGuildById("1219338270773874729").getTextChannelById("1246091381659668521");
            StringBuilder cmd = new StringBuilder("/" + event.getName() + " ");
            List<OptionMapping> options = event.getOptions();
            for (OptionMapping option : options) {
                cmd.append(option.getName()).append(":").append(option.getAsString()).append(" ");
            }
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Error Report")
                    .setDescription(String.format("Message: ```%s```\nAuthor Username: `%s`\nAuthor ID: `%s`", cmd, event.getUser().getName() + "#" + event.getUser().getDiscriminator(), event.getUser().getId()))
                    .setColor(EmbedColor.RED)
                    .build();
            try {
                Path temp = Files.createTempFile("error", ".txt");
                Files.writeString(temp, err);
            } catch (Exception L) {
                sys.exitWithError(String.format("VERY CRITICAL ERROR\n\n\nMessage: ```%s```\nAuthor Username: `%s`\nAuthor ID: `%s`",event.getName(), event.getUser().getName() + "#" + event.getUser().getDiscriminator(), event.getUser().getId()));
            }
            channel.sendMessage("<@" + event.getJDA().retrieveApplicationInfo().complete().getOwner().getId() + ">").addEmbeds(embed).addFiles(FileUpload.fromData(err.toString().getBytes(StandardCharsets.UTF_8), "error_report.txt")).queue();

        }
    }


    public void onButton(ButtonInteractionEvent event) throws IOException, ParseException {
        List<Command> commandsToExecute = new ArrayList<>();
        db.add("properties", "cmds", 1);

        try {
            for (File file : files) {
                if (file.getName().endsWith(".class")) {
                    Class<?> cls = Class.forName("spigey.bot." + file.getPath()
                            .replace(File.separator, ".")  // Replace file separators with dots
                            .substring(file.getPath().indexOf("spigey\\bot") + "spigey.bot".length() + 1) // Get class name after "spigey.bot."
                            .replace(".class", ""));
                    if (cls.isAnnotationPresent(CommandInfo.class)) {
                        CommandInfo info = cls.getAnnotation(CommandInfo.class);
                        if (info.buttonId().equalsIgnoreCase(event.getComponentId())) {
                            Command command = (Command) cls.getDeclaredConstructor().newInstance();
                            commandsToExecute.add(command);
                        } else if (info.buttonId().equals("%")) {
                            Command command = (Command) cls.getDeclaredConstructor().newInstance();
                            commandsToExecute.add(command);
                        }
                    }
                }
            }
        } catch (Exception e) {
            errInfo(e);
        }

        if (commandsToExecute.isEmpty()) {
            event.reply("Fatal error").setEphemeral(true).queue();
            return;
        }

        for (Command command : commandsToExecute) {
            try {
                if (command.getClass().isAnnotationPresent(CommandInfo.class)) {
                    CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);
                    if (info.limitIds().length > 0 && !Arrays.asList(info.limitIds()).contains(event.getUser().getId())) {
                        event.getChannel().sendMessage(info.limitMsg()).queue();
                        return;
                    }
                    if (Objects.equals(db.read(event.getUser().getId(), "banned"), "0")) {
                        command.button(event);
                    }
                }
            } catch (Exception e) {
                try{
                StringBuilder err = new StringBuilder(e + "\n   ");
                for (int i = 0; i < e.getStackTrace().length - 1; i++) {
                    err.append(e.getStackTrace()[i]).append("\n   ");
                }
                err.append(e.getStackTrace()[e.getStackTrace().length - 1]);
                error("A critical error has occurred while executing Command:\n" + e + "\nMessage: " + event.getMessage().getContentRaw(), false);
                event.reply("A critical error occurred while executing Command: ```" + (err.toString().length() > 1000 ? err.substring(0, 1000) + "..." : err.toString()) + "```\nThis error has been automatically reported.").setEphemeral(true).queue();
                TextChannel channel = event.getJDA().getGuildById("1219338270773874729").getTextChannelById("1246091381659668521");
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Error Report")
                        .setDescription(String.format("Message: ```%s```\nAuthor Username: `%s`\nAuthor ID: `%s`", event.getMessage().getContentRaw(), event.getUser().getName() + "#" + event.getUser().getDiscriminator(), event.getUser().getId()))
                        .setColor(EmbedColor.RED)
                        .build();
                    Path temp = Files.createTempFile("error", ".txt");
                    Files.writeString(temp, err);
                channel.sendMessage("<@" + event.getJDA().retrieveApplicationInfo().complete().getOwner().getId() + ">").addEmbeds(embed).addFiles(FileUpload.fromData(err.toString().getBytes(StandardCharsets.UTF_8), "error_report.txt")).queue();
                } catch (Exception L) {
                    sys.exitWithError(String.format("VERY CRITICAL ERROR\n\n\nMessage: ```%s```\nAuthor Username: `%s`\nAuthor ID: `%s`", event.getMessage().getContentRaw(), event.getUser().getName() + "#" + event.getUser().getDiscriminator(), event.getUser().getId()));
                }
            }
        }
    }

    private void loadDir(File dir) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                loadDir(file); // Recurse into subdirectory
            } else if (file.getName().endsWith(".class")) {
                String dot = ".";
                String className = "spigey.bot." + file.getPath()
                        .replace(File.separator, ".")  // Replace file separators with dots
                        .substring(file.getPath().indexOf("spigey\\bot") + "spigey.bot".length() + 1) // Get class name after "spigey.bot."
                        .replace(".class", "");        // Remove the ".class" extension
                Class<?> cls = Class.forName(className);
                if (Command.class.isAssignableFrom(cls)) {
                    Command command = (Command) cls.getDeclaredConstructor().newInstance();
                    String commandName = className.substring(className.lastIndexOf('.') + 1).replace("Command", "").toLowerCase();
                    debug("Registered command " + className, false);
                    commands.put(commandName, command);

                    // Read annotations
                    if (cls.isAnnotationPresent(CommandInfo.class)) {
                        CommandInfo info = cls.getAnnotation(CommandInfo.class);
                        for (String alias : info.aliases()) {
                            debug("Registered alias " + alias + " for command " + className, false);
                            aliasToCommandMap.put(alias.toLowerCase(), commandName);
                        }

                        // Register cooldown manager
                        if (info.cooldown() > 0) {
                            cooldownManagers.put(commandName, new CooldownManager(info.cooldown()));
                        }
                    }
                }
            }
        }
    }

    private File[] fileRecur(File dir) {
        List<File> filesList = new ArrayList<>();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                filesList.addAll(Arrays.asList(fileRecur(file)));
            } else if (file.getName().endsWith(".class")) {
                filesList.add(file);
            }
        }
        return filesList.toArray(new File[0]);
    }

    public void onMessageReceived(MessageReceivedEvent event) throws Exception {
        doTheActualShit(event);
    }
}
