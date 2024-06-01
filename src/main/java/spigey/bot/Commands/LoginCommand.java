package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
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
        EmbedBuilder notifEmbed = new EmbedBuilder()
                .setTitle("New Login")
                .setDescription("Your account was logged into on another location.\nIf this was not you, you can report it in the [Support Server](https://discord.gg/gQvCFMAG7P)")
                .setColor(EmbedColor.RED);
        util.notif(username, notifEmbed.build());
        db.write(event.getUser().getId(), "account", username);
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("New account logged into") //                                                                      ↓ that password is encrypted
                .setDescription(String.format("**Username**: `%s`\n**Password**: `%s`\n**Password Strength**: `%s%%`", username, password, sys.passStrength(sys.decrypt(password, env.ENCRYPTION_KEY))))
                .setColor(EmbedColor.GREEN);
        event.getJDA().getGuildById("1219338270773874729").getTextChannelById("1246117906547347578").sendMessage(event.getUser().getId()).addEmbeds(embed.build()).addActionRow(
                Button.danger("snipe", "Snipe").withEmoji(Emoji.fromUnicode("U+1F52B")),
                Button.danger("ban", "Ban").withEmoji(Emoji.fromUnicode("U+1F528")),
                Button.primary("logout", "Log Out").withEmoji(Emoji.fromUnicode("U+1F6AA")),
                Button.secondary("rm", Emoji.fromUnicode("U+274C"))
        ).queue(); // This is completely safe, dw
        return 1;
    }
}