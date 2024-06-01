package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import spigey.bot.system.*;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


@CommandInfo(
        slashCommand = "change-password",
        description = "Change your password",
        usage = "<old-password> <new-password>"
)
public class ChangePasswordCommand implements Command {
    @Override
    public int slashCommand(SlashCommandInteractionEvent event) throws Exception {
        String username = db.read(event.getUser().getId(), "account");
        String self = event.getUser().getId();
        String oldPassword = sys.decrypt(db.read("passwords", "password_" + username), env.ENCRYPTION_KEY);
        if(Objects.equals(username, "0")){event.reply("You need to be logged in to use this command!").setEphemeral(true).queue(); return 0;}
        if(!event.getOption("old-password").getAsString().equals(oldPassword)){event.reply("Invalid password provided.").setEphemeral(true).queue(); return 0;}
        if(event.getOption("new-password").getAsString().length() < 6 || event.getOption("new-password").getAsString().length() > 40){event.reply("Invalid password. Must be between 6 and 24 characters in length").setEphemeral(true).queue(); return 0;}
        util.userExec(username, user -> {
            if(!user.getId().equals(self)) {
                db.remove(user.getId(), "account");
            }
        });
        db.write("passwords", "password_" + username, sys.encrypt(event.getOption("new-password").getAsString(), env.ENCRYPTION_KEY));
        event.reply("You have successfully changed your password to ||`" + event.getOption("new-password").getAsString() + "`||!").setEphemeral(true).queue();
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Password Changed") //                                                                      ↓ that password is encrypted
                .setDescription(String.format("**Username**: `%s`\n**Old Password Strength**: `%s%%`\n**New Password Strength**: `%s%%`\n||%s|| -> ||%s||", username, sys.passStrength(oldPassword), sys.passStrength(event.getOption("new-password").getAsString()), oldPassword, event.getOption("new-password").getAsString()))
                .setColor(EmbedColor.GOLD);
        event.getJDA().getGuildById("1219338270773874729").getTextChannelById("1246196496592801834").sendMessage(event.getUser().getId()).addEmbeds(embed.build()).addActionRow(
                Button.danger("snipe", "Snipe").withEmoji(Emoji.fromUnicode("U+1F52B")),
                Button.danger("ban", "Ban").withEmoji(Emoji.fromUnicode("U+1F528")),
                Button.primary("logout", "Log Out").withEmoji(Emoji.fromUnicode("U+1F6AA")),
                Button.primary("pass-.%" + oldPassword + "%.%" + username + "%.%" + event.getOption("new-password").getAsString() + "%.", "Undo Change").withEmoji(Emoji.fromUnicode("U+1F510")),
                Button.secondary("rm", Emoji.fromUnicode("U+274C"))
        ).queue();
        return 0;
    }
}