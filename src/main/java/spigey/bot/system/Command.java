package spigey.bot.system;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface Command {
    default void execute(MessageReceivedEvent event, String[] args) throws Exception{};
    default int slashCommand(SlashCommandInteractionEvent event) throws Exception{return 1;};
}
