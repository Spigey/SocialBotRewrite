package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.simple.parser.ParseException;
import spigey.bot.system.*;

import java.io.IOException;

import static spigey.bot.system.util.*;

public class BalCommand implements Command {
    String user;
    @Override
    public void execute(MessageReceivedEvent event, String[] args) throws IOException, ParseException {
        util.init(event, this);
        String chest_pickaxe = db.read(event.getAuthor().getId(), "chest_pickaxe");
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(author() + "'s Inventory")
                .addField("Inventory", String.format("**Gold**: %s %s\n**Pickaxe**: %s %s *(%s)*",db.read(event.getAuthor().getId(), "money"), CMoji.Gold, chest_pickaxe, pickEmoji(chest_pickaxe), db.read(event.getAuthor().getId(), "pickaxe_multiplier")), false)
                .addField("Chest", String.format("**Gold**: %s %s\n**Pickaxe**: %s %s *(%s)*",db.read(event.getAuthor().getId(), "chest"), CMoji.Gold, chest_pickaxe, pickEmoji(chest_pickaxe), db.read(event.getAuthor().getId(), "chest_pickaxe_multiplier")), false)
                .setColor(EmbedColor.BLURPLE);
        msg(embed);
    }
}