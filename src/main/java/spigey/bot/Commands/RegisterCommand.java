package spigey.bot.Commands;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import spigey.bot.system.*;

import java.util.Objects;

@CommandInfo(
        slashCommand = "register"
)
public class RegisterCommand implements Command {
    @Override
    public int slashCommand(SlashCommandInteractionEvent event) throws Exception {
        if(event.getGuild() == null){event.reply("Commands are disabled in DMs, tough luck mguy").queue(); return 0;}
        String user = event.getUser().getId();
        String username = event.getOption("username").getAsString();
        String password = event.getOption("password").getAsString();
        if(!Objects.equals(db.read(event.getUser().getId(), "account"), "0")){event.reply("You are already registered!").setEphemeral(true).queue(); return 0;}
        if(!Objects.equals(db.read("passwords", "password_" + username), "0")){event.reply("There is already a user with that username!").setEphemeral(true).queue(); return 0;}
        if(!username.matches("^[a-zA-Z0-9_]*$") || username.length() < 3 || username.length() > 24){event.reply("Invalid username. Must be between 3 and 24 characters in length").setEphemeral(true).queue(); return 0;}
        if(password.length() < 6 || password.length() > 24){event.reply("Invalid password. Must be between 6 and 24 characters in length").setEphemeral(true).queue(); return 0;}
        db.write(user, "account", username);
        db.write("passwords", "password_" + username, sys.encrypt(password, env.ENCRYPTION_KEY));
        event.reply("You have successfully registered as `" + username + "`!").setEphemeral(true).queue();
        return 1;
    }
}