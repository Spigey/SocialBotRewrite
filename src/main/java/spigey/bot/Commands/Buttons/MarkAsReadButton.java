package spigey.bot.Commands.Buttons;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import spigey.bot.system.Command;
import spigey.bot.system.CommandInfo;
import spigey.bot.system.db;
import spigey.bot.system.util;

@CommandInfo(
        buttonId = "read"
)
public class MarkAsReadButton implements Command {
    @Override
    public void button(ButtonInteractionEvent event) throws Exception {
        db.remove(db.read(event.getUser().getId(), "account"), "notifications");
        event.reply("Successfully marked all notifications as read!").setEphemeral(true).queue();
    }
}