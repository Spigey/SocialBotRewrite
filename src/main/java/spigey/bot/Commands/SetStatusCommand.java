package spigey.bot.Commands;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.checker.units.qual.C;
import spigey.bot.system.Command;
import spigey.bot.system.CommandInfo;
import spigey.bot.system.db;
import spigey.bot.system.util;

import java.util.Objects;


@CommandInfo(
        slashCommand = "setstatus"
)
public class SetStatusCommand implements Command {
    @Override
    public int slashCommand(SlashCommandInteractionEvent event) throws Exception {
        if(event.getGuild() == null){event.reply("Commands are disabled in DMs, tough luck mguy").queue(); return 0;}
        if(Objects.equals(db.read(event.getUser().getId(), "account"), "0")){event.reply("You have to register first to post something!").setEphemeral(true).queue(); return 0;}
        String username = db.read(event.getUser().getId(), "account");
        if(event.getOption("status") == null){
            db.remove(username, "status");
            event.reply("You have successfully reset your status!").queue();
            return 1;
        }
        if(event.getOption("status").getAsString().length() > 100){event.reply("Your status must be at most 100 characters in length.").setEphemeral(true).queue();}
        db.write(username, "status", event.getOption("status").getAsString());
        event.reply("You have successfully successfully updated your status!").queue();
        return 1;
    }
}