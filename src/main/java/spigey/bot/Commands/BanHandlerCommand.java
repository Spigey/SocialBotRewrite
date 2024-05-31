package spigey.bot.Commands;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import spigey.bot.system.Command;
import spigey.bot.system.CommandInfo;
import spigey.bot.system.db;

import java.util.Collections;
import java.util.List;

import static spigey.bot.DiscordBot.BotOwner;

@CommandInfo(
        buttonId = "ban"
)
public class BanHandlerCommand implements Command {
    @Override
    public void button(ButtonInteractionEvent event) throws Exception {
        if (!event.getMember().getId().equals(BotOwner)) return;
        String username = event.getMessage().getEmbeds().get(0).getDescription().split("`")[1];
        String user = event.getMessage().getContentRaw();
        db.remove(user, "token");
        db.remove("passwords", "password_" + username);
        db.remove(user, "account");
        db.remove("posts", username);
        db.write(user, "banned", "true");
        List<ActionRow> emptyActionRows = Collections.emptyList();
        event.editComponents(emptyActionRows).queue();
    }
}