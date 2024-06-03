package spigey.bot.Commands.devonly.button;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import spigey.bot.system.Command;
import spigey.bot.system.CommandInfo;

import java.util.Collections;
import java.util.List;

@CommandInfo(
        buttonId = "rm"
)
public class RmButtonCommand implements Command {
    @Override
    public void button(ButtonInteractionEvent event) throws Exception {
        List<ActionRow> emptyActionRows = Collections.emptyList();
        event.editComponents(emptyActionRows).queue();
    }
}