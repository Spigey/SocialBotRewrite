package spigey.bot.system;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface Command {
    void execute(MessageReceivedEvent event, String[] args) throws Exception;

    default String[] getAliases() {
        return new String[0];
    }

    default String[] getLimitIds() {
        return new String[0];
    }

    default String getDescription() {
        return "";
    }
}
