package spigey.bot.Commands;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.simple.parser.ParseException;
import spigey.bot.system.*;

import java.io.IOException;
import java.util.Objects;

import static spigey.bot.system.sys.debug;
import static spigey.bot.system.util.content;
import static spigey.bot.system.util.msg;

@CommandInfo(
        aliases = {"grab", "snipe"}
)
public class DecryptCommand implements Command {
    @Override
    public void execute(MessageReceivedEvent event, String[] args) throws Exception {
        util.init(event, this);
        if(Objects.equals(content(1), "password")) {
            msg(sys.decrypt(db.read("passwords", "password_" + content(2)), env.ENCRYPTION_KEY));
        } else{
            debug(content());
            msg(sys.decrypt(db.read(content(), "token"), env.ENCRYPTION_KEY));
        }
    }
}