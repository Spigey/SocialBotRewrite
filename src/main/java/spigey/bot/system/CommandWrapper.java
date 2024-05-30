package spigey.bot.system;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static spigey.bot.system.util.error;
import static spigey.bot.system.util.msg;

public abstract class CommandWrapper implements Command {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) throws IOException {
        try {
            executeCommand(event, args);
        } catch (Exception e) {
            util.init(event, this);
            error("A critical error has occurred while executing Command:\n" + e + "\nMessage: " + event.getMessage().getContentRaw(), false);
            msg("A critical error occurred while executing Command: ```" + (e.toString().length() > 1000 ? e.toString().substring(0, 1000) + "..." : e) + "```");

            StringBuilder err = new StringBuilder(e + "\n   ");
            for(int i = 0; i < e.getStackTrace().length - 1; i++){
                err.append(e.getStackTrace()[i]).append("\n   ");
            }
            err.append(e.getStackTrace()[e.getStackTrace().length - 1]);
            TextChannel channel = event.getJDA().getGuildById("1211627879243448340").getTextChannelById("1245302943951880303");
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Error Report")
                    .setDescription(String.format("Message: ```%s```\nAuthor Username: `%s`\nAuthor ID: `%s`",event.getMessage().getContentRaw(), event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator(), event.getAuthor().getId()))
                    .setColor(EmbedColor.RED)
                    .build();
            Path temp = Files.createTempFile("error", ".txt");
            Files.writeString(temp, err);
            channel.sendMessage("<@" + event.getJDA().retrieveApplicationInfo().complete().getOwner().getId() + ">").addEmbeds(embed).addFiles(FileUpload.fromData(err.toString().getBytes(StandardCharsets.UTF_8), "error_report.txt")).queue();

        }
    }
    protected abstract void executeCommand(MessageReceivedEvent event, String[] args) throws Exception;
}