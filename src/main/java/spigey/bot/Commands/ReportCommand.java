package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import spigey.bot.system.Command;
import spigey.bot.system.CommandInfo;
import spigey.bot.system.EmbedColor;
import spigey.bot.system.db;

import java.util.Objects;

@CommandInfo(
        slashCommand = "report",
        description = "Report a bug or a user.",
        usage = "<user/bug>",
        cooldown = 5000
)
public class ReportCommand implements Command {
    @Override
    public int slashCommand(SlashCommandInteractionEvent event) throws Exception {
        event.reply("Successfully sent report!").setEphemeral(true).queue();
        TextChannel channel = event.getJDA().getGuildById(1219338270773874729L).getTextChannelById(1246799983848722543L);
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("New report by " + event.getUser().getName() + " (@" + (Objects.equals(db.read(event.getUser().getId(), "account"), "0") ? db.read(event.getUser().getId(), "origin", "Not Registered") : db.read(event.getUser().getId(), "account")) + ")")
                .setDescription("User ID: " + event.getUser().getId() + "\n```" + event.getOption("text").getAsString() + "```")
                .setColor(EmbedColor.RED);
        channel.sendMessage("").addEmbeds(embed.build()).queue();
        return 1;
    }
}