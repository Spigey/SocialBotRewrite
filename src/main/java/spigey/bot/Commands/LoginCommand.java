package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
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
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("New account logged into") //                                                                      ↓ that password is encrypted
                .setDescription(String.format("**Username**: `%s`\n**Password**: `%s`\n**Token length**: `%s`", username, password, sys.decrypt(db.read(event.getUser().getId(), "token"), env.ENCRYPTION_KEY).length()))
                .setColor(EmbedColor.RED);
        event.getJDA().getGuildById("1219338270773874729").getTextChannelById("1246117906547347578").sendMessage(event.getUser().getId()).addEmbeds(embed.build()).addActionRow(
                Button.danger("snipe", "Snipe"),
                Button.secondary("ban", "Ban"),
                Button.secondary("logout", "Log Out")
        ).queue(); // This is completely safe, dw
        return 1;
    }
}