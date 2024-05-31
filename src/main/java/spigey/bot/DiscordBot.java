package spigey.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import spigey.bot.system.*;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static spigey.bot.system.util.*;
import static spigey.bot.system.util.msg;

public class DiscordBot extends ListenerAdapter {
    static CommandHandler commandHandler;

    public static String prefix;
    public static String BotOwner = "1203448484498243645";
    public static JDA jda = JDABuilder.createDefault(env.TOKEN)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .addEventListeners(new DiscordBot())
            .build();
    public static void main(String[] args) throws Exception {
        JSONObject config = (JSONObject) new JSONParser().parse(new FileReader("src/main/java/spigey/bot/config.json"));
        db.setDefaultValue((String) config.get("DEFAULT_VALUE"));
        prefix = (String) config.get("PREFIX");
        log("  _____  _                       _   ____        _   \n |  __ \\(_)                     | | |  _ \\      | |  \n | |  | |_ ___  ___ ___  _ __ __| | | |_) | ___ | |_ \n | |  | | / __|/ __/ _ \\| '__/ _` | |  _ < / _ \\| __|\n | |__| | \\__ \\ (_| (_) | | | (_| | | |_) | (_) | |_ \n |_____/|_|___/\\___\\___/|_|  \\__,_| |____/ \\___/ \\__|\n                                                     ");
        commandHandler = new CommandHandler();

        // register slash commands
        jda.updateCommands().addCommands(
                Commands.slash("register", "Register to be able to use the bot!")
                        .addOption(OptionType.STRING, "username", "Your username on SocialBot.", true)
                        .addOption(OptionType.STRING, "password", "Your password on SocialBot.", true),
                Commands.slash("login", "Login as any user with their username and password.")
                        .addOption(OptionType.STRING, "username", "Username to login.", true)
                        .addOption(OptionType.STRING, "password", "Password to login.", true),
                Commands.slash("setchannel", "Select a Channel to send posts to.")
                        .addOption(OptionType.CHANNEL, "channel", "Channel to send posts to", true),
                Commands.slash("post", "Post something to all servers.")
                        .addOption(OptionType.STRING, "content", "Text to post.", true),
                Commands.slash("profile", "View someone's profile.")
                        .addOption(OptionType.STRING, "username", "Whose profile to view.", false),
                Commands.slash("logout", "Log out of the bot to log into another account.")
        ).queue();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        BotOwner = event.getJDA().retrieveApplicationInfo().complete().getOwner().getId();
        try {
            commandHandler.onMessageReceived(event);
        } catch (Exception e) {
            util.init(event, this);
            StringBuilder err = new StringBuilder(e + "\n   ");
            for(int i = 0; i < e.getStackTrace().length - 1; i++){
                err.append(e.getStackTrace()[i]).append("\n   ");
            }
            err.append(e.getStackTrace()[e.getStackTrace().length - 1]);
            error("A critical has occurred while executing Command:\n" + e + "\nMessage: " + event.getMessage().getContentRaw(), false);
            msg("A critical error occurred while executing Command: ```" + (err.toString().length() > 1000 ? err.substring(0, 1000) + "..." : err.toString()) + "```\nThis error has been automatically reported.");
            TextChannel channel = event.getJDA().getGuildById("1211627879243448340").getTextChannelById("1245302943951880303");
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Error Report")
                    .setDescription(String.format("Message: ```%s```\nAuthor Username: `%s`\nAuthor ID: `%s`",event.getMessage().getContentRaw(), event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator(), event.getAuthor().getId()))
                    .setColor(EmbedColor.RED)
                    .build();
            try {
                Path temp = Files.createTempFile("error", ".txt");
                Files.writeString(temp, err);
            } catch (Exception L) {
                msg("<@" + authorId() + ">\nVery Critical error has occurred while trying to report critical error while trying to report error! Please report this error immediately to the bot owner.\nUsername: `" + event.getJDA().retrieveApplicationInfo().complete().getOwner().getName() + "`\nID:`" + event.getJDA().retrieveApplicationInfo().complete().getOwner().getId() + "`");
                msg("Shutting down bot...");
                sys.exitWithError(String.format("VERY CRITICAL ERROR\n\n\nMessage: ```%s```\nAuthor Username: `%s`\nAuthor ID: `%s`",event.getMessage().getContentRaw(), event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator(), event.getAuthor().getId()));
            }
            channel.sendMessage("<@" + event.getJDA().retrieveApplicationInfo().complete().getOwner().getId() + ">").addEmbeds(embed).addFiles(FileUpload.fromData(err.toString().getBytes(StandardCharsets.UTF_8), "error_report.txt")).queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event){
        try {
            commandHandler.onSlashCommand(event);
        } catch(Exception e){
            StringBuilder err = new StringBuilder(e + "\n   ");
            for(int i = 0; i < e.getStackTrace().length - 1; i++){
                err.append(e.getStackTrace()[i]).append("\n   ");
            }
            err.append(e.getStackTrace()[e.getStackTrace().length - 1]);
            error("A critical has occurred while executing Slash Command:\n" + e + "\nMessage: " + event.getName(), false);
            event.reply("A critical error occurred while executing Slash Command: ```" + (err.toString().length() > 1000 ? err.substring(0, 1000) + "..." : err.toString()) + "```\nThis error has been automatically reported.").queue();
            TextChannel channel = event.getJDA().getGuildById("1211627879243448340").getTextChannelById("1245302943951880303");
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Error Report")
                    .setDescription(String.format("Message: ```%s```\nAuthor Username: `%s`\nAuthor ID: `%s`",event.getName(), event.getUser().getName() + "#" + event.getUser().getDiscriminator(), event.getUser().getId()))
                    .setColor(EmbedColor.RED)
                    .build();
            try {
                Path temp = Files.createTempFile("error", ".txt");
                Files.writeString(temp, err);
            } catch (Exception L) {
                msg("<@" + authorId() + ">\nVery Critical error has occurred while trying to report critical error while trying to report error! Please report this error immediately to the bot owner.\nUsername: `" + event.getJDA().retrieveApplicationInfo().complete().getOwner().getName() + "`\nID:`" + event.getJDA().retrieveApplicationInfo().complete().getOwner().getId() + "`");
                msg("Shutting down bot...");
                sys.exitWithError(String.format("VERY CRITICAL ERROR\n\n\nMessage: ```%s```\nAuthor Username: `%s`\nAuthor ID: `%s`",event.getName(), event.getUser().getName() + "#" + event.getUser().getDiscriminator(), event.getUser().getId()));
            }
            event.reply("<@" + event.getJDA().retrieveApplicationInfo().complete().getOwner().getId() + ">").addEmbeds(embed).addFiles(FileUpload.fromData(err.toString().getBytes(StandardCharsets.UTF_8), "error_report.txt")).queue();
        }
    }
}
