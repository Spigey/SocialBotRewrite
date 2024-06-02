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
        if (!event.getComponentId().startsWith("pass-.%")) return;
        String componentId = event.getComponentId();
        int startIndex = componentId.indexOf(".%") + 2;
        int endIndex = componentId.indexOf("%.", startIndex);
        String password = componentId.substring(startIndex, endIndex);
        startIndex = endIndex + 3;
        endIndex = componentId.indexOf("%.%", startIndex);
        String username = componentId.substring(startIndex, endIndex);
        startIndex = endIndex + 3;
        String newPassword = componentId.substring(startIndex);
        newPassword = newPassword.substring(0, newPassword.length() - 2);
        String user = event.getMessage().getContentRaw();
        db.write(username, "password", sys.encrypt(password,env.ENCRYPTION_KEY));
        event.reply("Successfully reverted password change!").setEphemeral(true).queue();
        event.editButton(event.getButton().asDisabled()).queue();
        MessageEmbed punishLog = new EmbedBuilder()
                .setTitle(":closed_lock_with_key: Password Reverted")
                .setDescription(String.format("%s (%s / %s)'s Password change has been reverted by %s.\n`%s` (%s%%) -> ||`%s`|| (%s%%)",username, user, event.getJDA().retrieveUserById(user).complete().getAsTag(), event.getUser().getName(), newPassword, sys.passStrength(newPassword), password, sys.passStrength(password)))
                .setColor(EmbedColor.BLUE).build();
        event.getJDA().getGuildById("1219338270773874729").getTextChannelById("1246129804344823899").sendMessage("").addEmbeds(punishLog).queue();
    }
}
