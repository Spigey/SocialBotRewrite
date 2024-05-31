package spigey.bot.system;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static spigey.bot.DiscordBot.prefix;
import static spigey.bot.system.sys.errInfo;
import static spigey.bot.system.util.debug;

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
            String path = "spigey/bot/Commands";
            files = new File(getClass().getClassLoader().getResource(path).getFile()).listFiles((dir, name) -> name.endsWith(".class"));
            if (files == null) return;

            for (File file : files) {
                String className = "spigey.bot.Commands." + file.getName().replace(".class", "");
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
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
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

    public void doTheActualShit(MessageReceivedEvent event) throws IOException {
        String[] split = event.getMessage().getContentRaw().split(" ");
        if (!split[0].startsWith(prefix)) return;

        String commandName = split[0].substring(1).toLowerCase();
        String resolvedCommandName = aliasToCommandMap.getOrDefault(commandName, commandName);
        Command command = commands.get(resolvedCommandName);

        if (command != null) {
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

    public void onSlashCommand(SlashCommandInteractionEvent event){
        Command command = null;
        try {
            for (File file : files) {
                if (file.getName().endsWith(".class")) {
                    Class<?> cls = Class.forName("spigey.bot.Commands." + file.getName().replace(".class", ""));
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
                if(Objects.equals(db.read(event.getUser().getId(), "banned"), "0")) success = command.slashCommand(event) == 1;

                // Update cooldown after successful execution
                if (cooldownManager != null) {
                    if(success) cooldownManager.update(event.getUser());
                }
            }
        } catch (Exception e) {
            errInfo(e);
        }
    }


    public void onButton(ButtonInteractionEvent event){
        Command command = null;
        try {
            for (File file : files) {
                if (file.getName().endsWith(".class")) {
                    Class<?> cls = Class.forName("spigey.bot.Commands." + file.getName().replace(".class", ""));
                    if (cls.isAnnotationPresent(CommandInfo.class)) {
                        CommandInfo info = cls.getAnnotation(CommandInfo.class);
                        if (info.buttonId().equalsIgnoreCase(event.getComponentId())) {
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
            event.reply("Fatal error").setEphemeral(true).queue();
            return;
        }
        try {
            if (command.getClass().isAnnotationPresent(CommandInfo.class)) {
                CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);
                if (info.limitIds().length > 0 && !Arrays.asList(info.limitIds()).contains(event.getUser().getId())) {event.getChannel().sendMessage(info.limitMsg()).queue(); return;}
                if(Objects.equals(db.read(event.getUser().getId(), "banned"), "0")) command.button(event);
            }
        } catch (Exception e) {
            errInfo(e);
        }
    }



    public void onMessageReceived(MessageReceivedEvent event) throws Exception {
        doTheActualShit(event);
    }
}
