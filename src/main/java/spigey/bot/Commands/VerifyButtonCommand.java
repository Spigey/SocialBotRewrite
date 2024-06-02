package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import spigey.bot.system.*;

import java.util.Objects;

@CommandInfo(
        buttonId = "verify"
)
public class VerifyButtonCommand implements Command {
    @Override
    public void button(ButtonInteractionEvent event) throws Exception {
        String username = event.getMessage().getEmbeds().get(0).getDescription().split("`")[1];
        String user = event.getMessage().getContentRaw();
        if(Objects.equals(db.read("verified", username), "0")){
            db.write(username, "verified", CMoji.Verified);
            event.reply("Successfully verified " + username + "!").setEphemeral(true).queue();
            event.editButton(event.getButton().withStyle(ButtonStyle.DANGER).withLabel("Un-Verify")).queue();
            MessageEmbed punishLog = new EmbedBuilder()
                    .setTitle(":white_check_mark: Verified")
                    .setDescription(String.format("%s (%s / %s) has been verified by %s!", username, user, event.getJDA().retrieveUserById(user).complete().getAsTag(), event.getUser().getName()))
                    .setColor(EmbedColor.GREEN).build();
            event.getJDA().getGuildById("1219338270773874729").getTextChannelById("1246129804344823899").sendMessage("").addEmbeds(punishLog).queue();
        }else{
            db.remove(username, "verified");
            event.reply("Successfully unverified " + username + ".").setEphemeral(true).queue();
            event.editButton(event.getButton().withStyle(ButtonStyle.SUCCESS).withLabel("Verify")).queue();
            MessageEmbed punishLog = new EmbedBuilder()
                    .setTitle(":x: Un-verified")
                    .setDescription(String.format("%s (%s / %s) has been un-verified by %s.", username, user, event.getJDA().retrieveUserById(user).complete().getAsTag(), event.getUser().getName()))
                    .setColor(EmbedColor.RED).build();
            event.getJDA().getGuildById("1219338270773874729").getTextChannelById("1246129804344823899").sendMessage("").addEmbeds(punishLog).queue();
        }
    }
}