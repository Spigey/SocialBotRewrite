package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import spigey.bot.system.Command;
import spigey.bot.system.CommandInfo;
import spigey.bot.system.EmbedColor;
import spigey.bot.system.db;

import java.util.Objects;

import static spigey.bot.DiscordBot.prefix;


@CommandInfo(
        buttonId = "%"
)
public class ReportButton implements Command {
    @Override
    public void button(ButtonInteractionEvent event) throws Exception {
        if(!event.getComponent().getId().startsWith("report_")) return;
        String reportedUser = event.getComponent().getId().replace("report_", "");
        MessageEmbed content = event.getMessage().getEmbeds().get(0);
        String user = db.read(event.getUser().getId(), "account");
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("New Report by @" + user)
                .setDescription("Reply to this message with " + prefix + "snipe to snipe this user.")
                .setColor(EmbedColor.RED);
        if(Objects.equals(user, "0")){event.reply("You need to be logged in to report this person!").setEphemeral(true).queue(); return;}
        event.reply("You have successfully reported " + reportedUser + "!").setEphemeral(true).queue();
        EmbedBuilder reportedEmbed = new EmbedBuilder()
                .setDescription("This post has been reported by: " + user)
                .setColor(EmbedColor.RED);
        if(event.getMessage().getEmbeds().get(1) != null){
            reportedEmbed = new EmbedBuilder(event.getMessage().getEmbeds().get(1))
                    .setDescription(event.getMessage().getEmbeds().get(1).getDescription() + ", " + user);
        }
        event.getMessage().editMessageEmbeds(content, reportedEmbed.build());
        TextChannel channel = event.getJDA().getGuildById("1219338270773874729").getTextChannelById("1246799983848722543");
        channel.sendMessage("").addEmbeds(embed.build(), content).queue();
    }
}