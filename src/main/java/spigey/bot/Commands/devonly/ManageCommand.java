package spigey.bot.Commands.devonly;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import spigey.bot.system.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static spigey.bot.DiscordBot.*;
import static spigey.bot.system.sys.*;

@CommandInfo(
        aliases = {"manage", "clear", "users", "chdel", "snipe", "encrypt", "decrypt", "rules", "delete"}
)
public class ManageCommand implements Command {
    @Override
    public void execute(MessageReceivedEvent event, String[] args) throws Exception {
        if(!"941366409399787580, 1203448484498243645, ".contains(event.getAuthor().getId() + ", ")){return;}
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
        if(args[0].equalsIgnoreCase(prefix + "delete")){
            db.deletePost(event.getMessage().getReferencedMessage().getContentRaw());
            event.getMessage().getReferencedMessage().delete().queue();
            try {
                String username = event.getMessage().getReferencedMessage().getEmbeds().get(1).getTitle().split("@")[1].split(" ")[0];
                StringBuffer sb = new StringBuffer();
                Matcher dkjdfh = Pattern.compile("<@!?(\\d+)>").matcher(event.getMessage().getReferencedMessage().getEmbeds().get(1).getDescription());
                while (dkjdfh.find()) {
                    try {
                        dkjdfh.appendReplacement(sb, "@" + jda.retrieveUserById(dkjdfh.group(1)).complete().getName());
                    } catch (ErrorResponseException L) {
                        dkjdfh.appendReplacement(sb, "@unkno...");
                    }
                }
                dkjdfh.appendTail(sb);
                String reustl = Arrays.stream(sb.toString().split("\\s+"))
                        .map(word -> badWords.stream().anyMatch(bad -> word.toLowerCase().contains(bad))
                                ? word.replaceAll(".", "?")
                                : word)
                        .collect(Collectors.joining(" "));
                db.write(username, "posts", Pattern.compile("\\[" + Pattern.quote(reustl) + "]\\[(.*?)]").matcher(db.read(username, "posts")).replaceAll(""));
            }catch(Exception L){sys.errInfo(L);}
            return;
        }
        String user = args[1].replaceAll("--self", event.getAuthor().getId());
        if(args[0].equalsIgnoreCase(prefix + "users")){util.userExecF(args[1]).thenAccept(users -> {StringBuilder usersString = new StringBuilder();
            for (User userr : users) {
                usersString.append(String.format("%s (%s), ", userr.getAsTag(), userr.getId()));
            } event.getChannel().sendMessage(usersString.substring(0, usersString.length() - 2)).queue();}); return;}
        String username = db.read(user, "account", "???");
        String password = db.read(username, "password", "???");
        AtomicReference<String> decryptedPassword = new AtomicReference<>(db.read(username, "password", "???"));
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
                if(flag(args, "-v")) decryptedPassword.set(sys.decrypt(decryptedPassword.get(), env.ENCRYPTION_KEY));
                String followers = String.valueOf(occur(db.read(username, "followers", "???"), ", "));
                String following = String.valueOf(occur(db.read(username, "following", "???"), ", "));
                if(flag(args, "-c")){
                    String[] followerusers = db.read(username, "followers", "").split(", ");
                    String userString = db.read(username, "followers", "");
                    for(String thisuser : followerusers){
                        if(!util.userExists(thisuser)){
                            userString = userString.replace(thisuser + ", ", ", ");
                        }
                    }
                    followers += " -> " + userString.replaceAll(" , ", " ").split(", ").length;
                    db.write(username, "followers", userString.replaceAll(" , ", " "));
                    userString = db.read(username, "following", "");
                    followerusers = db.read(username, "following", "").split(", ");
                    for(String thisuser : followerusers){
                        if(!util.userExists(thisuser)){
                            userString = userString.replace(thisuser + ", ", ", ");
                        }
                    }
                    db.write(username, "following", userString.replaceAll(" , ", " "));
                    following += " -> " + userString.replaceAll(" , ", " ").split(", ").length;
                }
                embed = new EmbedBuilder()
                        .setTitle("Account management Panel")
                        .setDescription(String.format("""
                                **Username**: `%s` %s%s
                                **User**: `%s`
                                **Status**: `%s`
                                **Last Online**: <t:%s:R>
                                **Password**: `%s`
                                **Decrypted Password**: ||`%s`||
                                **Password Strength**: `%s%%`
                                **Token length**: `%s`
                                **Followers**: (%s) `%s `
                                **Following**: (%s) `%s `
                                **Origin**: `%s`
                                **Users**: `%s`
                                **Posts**: %s""", username, db.read(username, "verified", ""), db.read(user, "banned", "false").replace("true", EmojiDB.Banned), finalUser, db.read(username, "status", "///"), db.read(username, "last_online"), sys.passToStr(password, "*"), decryptedPassword, sys.passStrength(decryptedPassword.get()), sys.decrypt(db.read(user, "token", ""), env.ENCRYPTION_KEY).length(), followers, db.read(username, "followers", "???"), following, db.read(username, "following", "???"), db.read(user, "origin", "???"), usersString, db.read(username, "posts", "`???`")))
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
    private boolean flag(String[] content, String flag) {
        for(String arg : content){
            if(Objects.equals(arg, content[0])) continue;
            if(arg.equals(flag)) return true;
        }
        return false;
    }
}