package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import spigey.bot.system.*;

@CommandInfo(
        buttonId = "%"
)
public class UndoPasswordCommand implements Command {
    @Override
    public void button(ButtonInteractionEvent event) throws Exception {
        if(!event.getComponentId().startsWith("pass-.%")) return;
        String password = event.getComponentId().split(".%")[1].split("%.")[0];
        String username = event.getComponentId().split(".%")[2].split("%.")[0];
        String newPassword = event.getComponentId().split(".%")[3].split("%.")[0];
        String user = event.getMessage().getContentRaw();
        db.write("passwords", "password_" + username, password);
        event.reply("Successfully reverted password change!").setEphemeral(true).queue();
        event.editButton(event.getButton().asDisabled()).queue();
        MessageEmbed punishLog = new EmbedBuilder()
                .setTitle(":closed_lock_with_key: Password Reverted")
                .setDescription(String.format("%s (%s)'s Password change has been reverted by %s.\n`%s` (%s%%) -> ||`%s`|| (%s%%)",username, user, event.getUser().getName(), newPassword, sys.passStrength(newPassword), password, sys.passStrength(password)))
                .setColor(EmbedColor.BLUE).build();
        event.getJDA().getGuildById("1219338270773874729").getTextChannelById("1246129804344823899").sendMessage("").addEmbeds(punishLog).queue();
    }
}
