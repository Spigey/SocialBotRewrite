package spigey.bot.Commands;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import spigey.bot.system.Command;
import spigey.bot.system.CommandInfo;
import spigey.bot.system.db;

import static spigey.bot.DiscordBot.BotOwner;

@CommandInfo(
        buttonId = "logout"
)
public class LogOutHandlerCommand implements Command {
    @Override
    public void button(ButtonInteractionEvent event) throws Exception {
        if (!event.getMember().getId().equals(BotOwner)) return;
        String user = event.getMessage().getContentRaw();
        db.remove(user, "account");
        event.editButton(event.getButton().asDisabled()).queue();
    }
}