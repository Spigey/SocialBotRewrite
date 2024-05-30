package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.simple.parser.ParseException;
import spigey.bot.system.*;

import java.io.IOException;

import static spigey.bot.system.util.*;
@CommandInfo(
        aliases = {"mine"},
        cooldown = 30000
)
public class WorkCommand implements Command {
    @Override
    public void execute(MessageReceivedEvent event, String[] args) throws IOException, ParseException {
        init(event, this);
        int random = (int) (Math.random() * 30);
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(author() + " is working!")
                .setDescription("You went working and have received " + random + "$ as a reward!")
                .setColor(EmbedColor.BLURPLE);
        msg(embed);
        try {
            db.add(event.getAuthor().getId(), "money", random);
            if(Integer.parseInt(db.read(event.getAuthor().getId(), "money")) > 9999999) db.write(event.getAuthor().getId(), "money", "-2147483647");
        } catch(NumberFormatException L){
            EmbedBuilder errEmbed = new EmbedBuilder()
                    .setTitle("Database Corruption Detected!")
                    .setDescription("It has been detected that the database is corrupted. Your money was automatically reset to 0.\nThis incident will be quickly reviewed and resolved by the bot owner.")
                    .setColor(EmbedColor.RED);
            TextChannel channel = event.getJDA().getGuildById("1211627879243448340").getTextChannelById("1245302943951880303");
            channel.sendMessage(String.format("Database Corruption Detected!\n  User:%s\n  Money:%s", event.getAuthor().getId(), db.read(event.getAuthor().getId(), "money"))).queue();
            msg(errEmbed);
            sys.warn(String.format("Database Corruption Detected!\n  User:%s\n  Money:%s", event.getAuthor().getId(), db.read(event.getAuthor().getId(), "money")));

            db.write(event.getAuthor().getId(), "money", "0");
        }
    }
}