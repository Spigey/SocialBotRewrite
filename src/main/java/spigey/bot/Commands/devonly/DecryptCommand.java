package spigey.bot.Commands.devonly;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import spigey.bot.system.*;

import java.util.Objects;

import static spigey.bot.system.util.content;
import static spigey.bot.system.util.msg;

@CommandInfo(
        aliases = {"ez"}
)
public class DecryptCommand implements Command {
    @Override
    public void execute(MessageReceivedEvent event, String[] args) throws Exception {
        util.init(event, this);
        if(args.length == 0) return;
        if(Objects.equals(args[1], "token")) {
            msg(sys.decrypt(db.read(args[2], "token"), env.ENCRYPTION_KEY));
        } else{
            msg(sys.decrypt(db.read(args[1], "password"), env.ENCRYPTION_KEY));
        }
    }
}