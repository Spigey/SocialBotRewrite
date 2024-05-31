package spigey.bot.Commands;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import spigey.bot.system.*;

import java.util.Objects;

@CommandInfo(
        slashCommand = "login"
)
public class LoginCommand implements Command {
    @Override
    public int slashCommand(SlashCommandInteractionEvent event) throws Exception {
        if(event.getGuild() == null){event.reply("Commands are disabled in DMs, tough luck mguy").queue(); return 0;}
        if(!Objects.equals(db.read(event.getUser().getId(), "account"), "0")){event.reply("You are already logged in!").setEphemeral(true).queue(); return 0;}
        String username = event.getOption("username").getAsString();
        String password = sys.encrypt(event.getOption("password").getAsString(), env.ENCRYPTION_KEY);
        // if(Objects.equals(db.read("passwords", "password_" + username), "0")){event.reply("There is no user with that username!").setEphemeral(true).queue(); return;}
        if(!password.equals(db.read("passwords", "password_" + username))){event.reply("Invalid username or password.").setEphemeral(true).queue(); return 0;}
        event.reply("You have successfully logged in as `" + username + "`!").setEphemeral(true).queue();
        db.write(event.getUser().getId(), "account", username);
        return 1;
    }
}