package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import spigey.bot.system.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static spigey.bot.DiscordBot.prefix;

@CommandInfo(
        aliases = {"manage", "clear"}
)
public class ManageCommand implements Command {
    @Override
    public void execute(MessageReceivedEvent event, String[] args) throws Exception {
        if(!"941366409399787580, 787626092248170506, 1203448484498243645, ".contains(event.getAuthor().getId() + ", ")){return;}
        if(args[0].equalsIgnoreCase(prefix + "clear")){event.getChannel().getHistory().retrievePast(100).queue(messages -> {
            event.getChannel().purgeMessages(messages);
        }); return;}
        String user = args[1].replaceAll("--self", event.getAuthor().getId());
        String username = db.read(user, "account", "???");
        String password = db.read("passwords", "password_" + username, "???");
        String decryptedPassword = sys.decrypt(db.read("passwords", "password_" + username, "???"),env.ENCRYPTION_KEY);
        String User = "???";
        try{
            User = String.format("%s (%s)", event.getJDA().retrieveUserById(user).complete().getAsTag(), user);
        }catch(Exception L){/**/}
        CompletableFuture<List<User>> userFuture = util.userExecF(username);

        String finalUser = User;
        userFuture.thenAccept(users -> {
            StringBuilder usersString = new StringBuilder();
            for (User userr : users) {
                usersString.append(String.format("%s (%s), ", userr.getAsTag(), userr.getId()));
            }
            EmbedBuilder embed = null;
            try {
                embed = new EmbedBuilder()
                        .setTitle("Account management Panel")
                        .setDescription(String.format("**Username**: `%s` %s\n**User**: `%s`\n**Password**: `%s`\n**Decrypted Password**: ||`%s`||\n**Password Strength**: `%s%%`\n**Token length**: `%s`\n**Users**: `%s`", username, db.read("verified", username, ""), finalUser, sys.passToStr(password, "*"), decryptedPassword, sys.passStrength(decryptedPassword), sys.decrypt(db.read(user, "token", ""), env.ENCRYPTION_KEY).length(), usersString))
                        .setColor(EmbedColor.RED);
            } catch (Exception e) {/**/}
            event.getMessage().reply(user).addEmbeds(embed.build()).addActionRow(
                    Button.danger("snipe", "Snipe"),
                    Button.danger("ban", "Ban"),
                    Button.success("verify", "Verify"),
                    Button.secondary("logout", "Log Out")
            ).queue();
        });
    }
}