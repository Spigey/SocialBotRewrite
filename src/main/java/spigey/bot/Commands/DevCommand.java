package spigey.bot.Commands;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.simple.parser.ParseException;
import spigey.bot.system.Command;
import spigey.bot.system.CommandInfo;
import spigey.bot.system.*;

import java.io.IOException;
import java.util.Objects;

import static spigey.bot.system.util.*;

@CommandInfo(
        limitIds = {"1203448484498243645", "1128164873554112513", "787626092248170506", "787626092248170506"}
)
public class DevCommand implements Command {
    @Override
    public void execute(MessageReceivedEvent event, String[] args) throws IOException, ParseException {
        util.init(event, this);
        String ch = content(1);
        if(Objects.equals(ch, "write")){
            db.write(content(2), content(3), content(4));
            msg(String.format("Set %s's %s to %s", event.getJDA().getUserById(content(2)).getName(), content(3), content(4)));
        } else if(Objects.equals(ch, "read")){
            msg(String.format("%s has %s %s", event.getJDA().getUserById(content(2)).getName(), db.read(content(2), content(3)), content(3)));
        } else if(Objects.equals(ch, "add")) {
            db.add(content(2), content(4), Integer.parseInt(content(3)));
            msg(String.format("Added %s %s to %s", content(4), content(3), event.getJDA().getUserById(content(2)).getName()));
        } else if(Objects.equals(ch, "exec")){
            if(authorId().equals("1203448484498243645")) msg(sys.exec(content().replace(content(1), "")));
        } else if(Objects.equals(ch, "reload")){
            new CommandHandler().reloadCommands();
            msg("Reloading commands");
        }
    }
    public void slashCommand(SlashCommandInteractionEvent event, String[] args){

    }
}