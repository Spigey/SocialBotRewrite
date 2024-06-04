package spigey.bot.Commands.devonly;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import spigey.bot.system.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static spigey.bot.DiscordBot.prefix;
import static spigey.bot.system.sys.errInfo;
import static spigey.bot.system.sys.error;

@CommandInfo(
        aliases = {"manage", "clear", "users", "chdel", "snipe", "encrypt", "decrypt", "rules"}
)
public class ManageCommand implements Command {
    @Override
    public void execute(MessageReceivedEvent event, String[] args) throws Exception {
        if(!"941366409399787580, 787626092248170506, 1203448484498243645, ".contains(event.getAuthor().getId() + ", ")){return;}
        if(args[0].equalsIgnoreCase(prefix + "chdel")){event.getChannel().delete().queue(); return;}
        if(args[0].equalsIgnoreCase(prefix + "clear")){event.getChannel().getHistory().retrievePast(100).queue(messages -> {
            event.getChannel().purgeMessages(messages);
        }); return;}
        if(args[0].equalsIgnoreCase(prefix + "snipe") && event.getChannel().getId().equals("1246799983848722543")){
            String usrnme = event.getMessage().getReferencedMessage().getEmbeds().get(1).getTitle().split("@")[1].split(" ")[0];
            db.remove(usrnme);
            util.userExec(usrnme, USER -> {
                db.remove(USER.getId(), "account");
                db.remove(USER.getId(), "token");
            });
            MessageEmbed punishLog = new EmbedBuilder()
                    .setTitle(":gun: Sniped!")
                    .setDescription(String.format("%s has been sniped by %s!",usrnme, event.getAuthor().getName()))
                    .setColor(EmbedColor.RED).build();
            event.getJDA().getGuildById("1219338270773874729").getTextChannelById("1246129804344823899").sendMessage("").addEmbeds(punishLog).queue();
            return;}
        if(args[0].equalsIgnoreCase(prefix + "encrypt")){
            event.getChannel().sendMessage(sys.encrypt(args[1], env.ENCRYPTION_KEY)).queue();
            return;}
        if(args[0].equalsIgnoreCase(prefix + "decrypt")){
            event.getChannel().sendMessage(sys.decrypt(args[1], env.ENCRYPTION_KEY)).queue();
            return;}
        if(args[0].equalsIgnoreCase(prefix + "rules")){
            EmbedBuilder tempEmbed = new EmbedBuilder()
                    .setTitle(":wave: Welcome to the Social Bot Support Server!")
                    .setDescription("\n" +
                            "We're here to help you with any bot questions, troubleshooting, or feedback. To keep things running smoothly, please follow these simple rules:\n" +
                            "\n" +
                            "### 2. Stay On Topic:\n" +
                            "- **Support Channels:** Use these for bot-related questions, errors, or suggestions.\n" +
                            "- **Off-Topic Channels:** We have spaces to chat and hang out. Keep bot talk in the right places.\n" +
                            "\n" +
                            "### 3. No Spam or Self-Promotion:\n" +
                            "- **Spamming:** Don't flood channels with repetitive messages or useless content.\n" +
                            "- **Advertising:** Want to share your own project? Ask a mod first.\n" +
                            "\n" +
                            "### 4. Follow Discord's Terms of Service:\n" +
                            "- **Illegal Activity:** This is a no-go zone.\n" +
                            "- **Exploits:** Don't try to break our bot or Discord itself.\n" +
                            "- **NSFW Content:** No.\n" +
                            "\n" +
                            "### 5. Listen to the Moderation Team:\n" +
                            "- Our mods are here to help. If you have a problem, reach out to them privately.\n" +
                            "- **Warnings and Bans:** Disregarding these rules may result in temporary or permanent removal from the server.")
                    .setColor(EmbedColor.PURPLE);
            event.getChannel().sendMessage("").addEmbeds(tempEmbed.build()).queue();
        }
        String user = args[1].replaceAll("--self", event.getAuthor().getId());
        if(args[0].equalsIgnoreCase(prefix + "users")){util.userExecF(args[1]).thenAccept(users -> {StringBuilder usersString = new StringBuilder();
            for (User userr : users) {
                usersString.append(String.format("%s (%s), ", userr.getAsTag(), userr.getId()));
            } event.getChannel().sendMessage(usersString.substring(0, usersString.length() - 2)).queue();}); return;}
        String username = db.read(user, "account", "???");
        String password = db.read(username, "password", "???");
        AtomicReference<String> decryptedPassword = new AtomicReference<>(sys.decrypt(db.read(username, "password", "???"), env.ENCRYPTION_KEY));
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
                try{usersString = new StringBuilder(usersString.substring(0, usersString.length() - 2));}catch(Exception L){error("Failed to retrieve users for ID " + user);}
                if(args.length > 2 && args[2].equalsIgnoreCase("-v")) decryptedPassword.set(sys.encrypt(decryptedPassword.get(), env.ENCRYPTION_KEY));
                embed = new EmbedBuilder()
                        .setTitle("Account management Panel")
                        .setDescription(String.format("**Username**: `%s` %s\n**User**: `%s`\n**Password**: `%s`\n**Decrypted Password**: ||`%s`||\n**Password Strength**: `%s%%`\n**Token length**: `%s`\n**Origin**: `%s`\n**Users**: `%s`", username, db.read(username, "verified", ""), finalUser, sys.passToStr(password, "*"), decryptedPassword, sys.passStrength(decryptedPassword.get()), sys.decrypt(db.read(user, "token", ""), env.ENCRYPTION_KEY).length(), db.read(user, "origin", "???"), usersString))
                        .setColor(EmbedColor.RED);
            } catch (Exception e) {errInfo(e);}
            event.getMessage().reply(user).addEmbeds(embed.build()).addActionRow(
                    Button.danger("snipe", "Snipe"),
                    Button.danger("ban", "Ban"),
                    Button.success("verify", "Verify"),
                    Button.secondary("logout", "Log Out")
            ).queue();
        });
    }
}