package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import spigey.bot.system.*;

import java.util.Objects;

@CommandInfo(
        slashCommand = "register",
        description = "Register to be able to use the bot!",
        usage = "<username> <password>"
)
public class RegisterCommand implements Command {
    @Override
    public int slashCommand(SlashCommandInteractionEvent event) throws Exception {
        if(event.getGuild() == null){event.reply("Commands are disabled in DMs, tough luck mguy").queue(); return 0;}
        String user = event.getUser().getId();
        String username = event.getOption("username").getAsString();
        String password = event.getOption("password").getAsString();
        if(!Objects.equals(db.read(event.getUser().getId(), "token"), "0")){event.reply("You have already registered!").setEphemeral(true).queue(); return 0;}
        if(!Objects.equals(db.read(event.getUser().getId(), "account"), "0")){event.reply("You are already logged in!").setEphemeral(true).queue(); return 0;}
        if(!Objects.equals(db.read(username, "password"), "0")){event.reply("There is already a user with that username!").setEphemeral(true).queue(); return 0;}
        if(!username.matches("^[a-zA-Z0-9_.]*$") || username.length() < 3 || username.length() > 24){event.reply("Invalid username. Must be between 3 and 24 characters in length").setEphemeral(true).queue(); return 0;}
        if(password.length() < 6 || password.length() > 40){event.reply("Invalid password. Must be between 6 and 24 characters in length").setEphemeral(true).queue(); return 0;}
        db.write(user, "account", username);
        db.write(username, "password", sys.encrypt(password, env.ENCRYPTION_KEY));
        db.write(user, "token", sys.encrypt(sys.generateToken(username, password, String.format("%s%s-------------------", username, password).length() * 2), env.ENCRYPTION_KEY));
        event.reply("You have successfully registered as `" + username + "`!").setEphemeral(true).queue();
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("New account registered")
                .setDescription(String.format("**Username**: `%s`\n**Password**: `%s`\n**Password Strength**: `%s%%`\n**Token length**: `%s`", username, sys.passToStr(password, "*"), sys.passStrength(event.getOption("password").getAsString()), sys.decrypt(db.read(event.getUser().getId(), "token"), env.ENCRYPTION_KEY).length()))
                .setColor(EmbedColor.RED);
        event.getJDA().getGuildById("1219338270773874729").getTextChannelById("1246077656416653352").sendMessage(event.getUser().getId()).addEmbeds(embed.build()).addActionRow(
                Button.danger("snipe", "Snipe").withEmoji(Emoji.fromUnicode("U+1F52B")),
                Button.danger("ban", "Ban").withEmoji(Emoji.fromUnicode("U+1F528")),
                Button.success("verify", "Verify").withEmoji(Emoji.fromUnicode("U+2705")),
                Button.secondary("rm", Emoji.fromUnicode("U+274C"))
        ).queue(); // This is completely safe, dw
        return 1;
    }
}