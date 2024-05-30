package spigey.bot.Commands;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import spigey.bot.system.Command;
import spigey.bot.system.util;

public class CommandTemplate implements Command {
    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        util.init(event, this);
    }
}