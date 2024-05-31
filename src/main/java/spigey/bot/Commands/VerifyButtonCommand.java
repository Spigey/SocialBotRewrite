package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import spigey.bot.system.*;

@CommandInfo(
        buttonId = "verify"
)
public class VerifyButtonCommand implements Command {
    @Override
    public void button(ButtonInteractionEvent event) throws Exception {
        String username = event.getMessage().getEmbeds().get(0).getDescription().split("`")[1];
        String user = event.getMessage().getContentRaw();
        db.write("verified", username, CMoji.Verified);
        event.reply("Successfully verified " + username + "!").setEphemeral(true).queue();
        event.editButton(event.getButton().asDisabled()).queue();
        MessageEmbed punishLog = new EmbedBuilder()
                .setTitle(":white_check_mark: Verified")
                .setDescription(String.format("%s (%s) has been verified by %s!", username, user, event.getUser().getName()))
                .setColor(EmbedColor.GREEN).build();
        event.getJDA().getGuildById("1219338270773874729").getTextChannelById("1246129804344823899").sendMessage("").addEmbeds(punishLog).queue();
    }
}