package spigey.bot.system;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


import java.io.FileReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.fusesource.leveldbjni.JniDBFactory.asString;
import static spigey.bot.DiscordBot.*;
import static spigey.bot.system.db.getDocument;

public class util {
    private static MessageReceivedEvent eventt;
    private static Object classs;
    public static void init(MessageReceivedEvent event, Object Class){
        eventt = event;
        classs = Class;
    }
    public static String content(){
        if(eventt.getMessage().getContentRaw().length() < (classs.getClass().getSimpleName().replace("Command","").length() + prefix.length() - 2)) return "";
        return eventt.getMessage().getContentRaw().substring((classs.getClass().getSimpleName().replace("Command","").length() + prefix.length() - 2)).trim();
    }
    public static String content(int index){
        String out;
        try{out = eventt.getMessage().getContentRaw().split(" ")[index];}
        catch(Exception L){return null;}
        return eventt.getMessage().getContentRaw().split(" ")[index];
    }
    public static void msg(String content){
        if(content == null) {eventt.getChannel().sendMessage("null").queue(); return;}
        if(content.isEmpty()) {eventt.getChannel().sendMessage("Possible IllegalStateException. Please report this bug.").queue(); return;}
        eventt.getChannel().sendMessage(content.replaceAll(env.TOKEN, "Fuck nuh uh")).queue();
    }
    public static void msg(EmbedBuilder embed){
        if(embed == null) embed = new EmbedBuilder().setDescription("null");
        eventt.getChannel().sendMessage("").setEmbeds(embed.build()).queue();
    }
    public static void msg(String content, EmbedBuilder embed){
        if(embed == null) embed = new EmbedBuilder().setDescription("null");
        if(content == null) content = "null";
        eventt.getChannel().sendMessage(content).setEmbeds(embed.build()).queue();
    }
    public static String author(){
        return eventt.getMember().getEffectiveName();
    }
    public static String authorId(){
        return eventt.getAuthor().getId();
    }
    public static void log(String content){
        System.out.println(content);
    }
    public static void debug(Object content, boolean chat){ // basically for gemini to understand that this will be debug, not basic output
        // log("\u001B[42;30m[DEBUG]: " + content + " \u001B[49m");
        log.info(content.toString());
        if(chat) msg("```\n" + content + "\n```");
    }
    public static void error(Object content, boolean chat) {
        // log("\u001B[41;30m[ERROR]: " + content + "\u001B[0m");
        log.error(content.toString());
        if(chat) msg("```\n" + content + "\n```");
    }
    public static void warn(Object content, boolean chat) {
        // log("\u001B[43;30m[WARN]: " + content + "\u001B[0m");
        log.warn(content.toString());
        if(chat) msg("```\n" + content + "\n```");
    }

    public static String getOwner() {
        if (jda == null) return null;
        return jda.retrieveApplicationInfo().complete().getOwner().getId();
    }
    public static String pickEmoji(String pickaxe){
        String out;
        switch(pickaxe){
            case "Wooden Pickaxe":
                out = EmojiDB.WoodenPickaxe;
                break;
            case "Stone Pickaxe":
                out = EmojiDB.StonePickaxe;
                break;
            case "Iron Pickaxe":
                out = EmojiDB.IronPickaxe;
                break;
            case "Diamond Pickaxe":
                out = EmojiDB.DiamondPickaxe;
                break;
            case "Golden Pickaxe":
                out = EmojiDB.GoldPickaxe;
            case "Netherite Pickaxe":
                out = EmojiDB.NetheritePickaxe;
                break;
            case "Apologies Pickaxe":
                out = EmojiDB.ApologiesPickaxe;
                break;
            case "Dev Pickaxe":
                out = EmojiDB.DevPickaxe;
                break;
            case "Premium Pickaxe":
                out = EmojiDB.PremiumPickaxe;
                break;
            default:
                out = EmojiDB.NoPickaxe;
        }
        return out;
    }

    private static DB dbase = db.retrieve();

    public static boolean notif(String username, MessageEmbed embed) {
        boolean found = false;
        try (DBIterator iterator = dbase.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String userId = asString(iterator.peekNext().getKey());
                JSONObject userObject = getDocument(userId);
                if (userObject.containsKey("account") && userObject.get("account").equals(username)) {
                    User user = jda.retrieveUserById(userId).complete();
                    if (user != null) {
                        user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("").addEmbeds(embed).queue());
                        found = true;
                    }
                }
            }
        } catch (Exception e) {
            sys.errInfo(e);
        }
        return found;
    }

    public static boolean userExists(String username) {
        return !db.read(username, "password", "").isEmpty();
    }

    public static void notif(String username, MessageEmbed embed, Button... buttons) {
        try (DBIterator iterator = dbase.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String userId = asString(iterator.peekNext().getKey());
                JSONObject userObject = getDocument(userId);
                if (userObject.containsKey("account") && userObject.get("account").equals(username)) {
                    User user = jda.retrieveUserById(userId).complete();
                    if (user != null) {
                        user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("").addEmbeds(embed).addActionRow(buttons).queue());
                    }
                }
            }
        } catch (Exception e) {
            sys.errInfo(e);
        }
    }

    public static CompletableFuture<List<User>> userExecF(String username) {
        CompletableFuture<List<User>> future = new CompletableFuture<>();
        List<User> retrievedUsers = new ArrayList<>();
        List<CompletableFuture<User>> userFutures = new ArrayList<>();

        CompletableFuture.runAsync(() -> {
            try (DBIterator iterator = dbase.iterator()) {
                for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                    String userId = asString(iterator.peekNext().getKey());
                    JSONObject userObject = getDocument(userId);
                    if (userObject.containsKey("account") && (username.equals("*") || userObject.get("account").equals(username))) {
                        RestAction<User> userAction = jda.retrieveUserById(userId);
                        CompletableFuture<User> userFuture = new CompletableFuture<>();
                        userAction.queue(userFuture::complete, userFuture::completeExceptionally);
                        userFutures.add(userFuture);
                        break;
                    }
                }

                CompletableFuture.allOf(userFutures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> {
                            for (CompletableFuture<User> userFuture : userFutures) {
                                try {
                                    retrievedUsers.add(userFuture.join());
                                } catch (Exception e) {
                                    sys.errInfo(e);
                                }
                            }
                            return retrievedUsers;
                        })
                        .thenAccept(future::complete)
                        .exceptionally(ex -> {
                            future.completeExceptionally(ex);
                            return null;
                        });
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    public static boolean userExec(String username, Consumer<User> action) {
        boolean found = false;
        try (DBIterator iterator = dbase.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String userId = asString(iterator.peekNext().getKey());
                JSONObject userObject = getDocument(userId);
                if (userObject.containsKey("account") && (userObject.get("account").equals(username))) {
                    User user = jda.retrieveUserById(userId).complete();
                    if (user != null) {
                        action.accept(user);
                        found = true;
                    }
                }
            }
        } catch (Exception e) {
            sys.errInfo(e);
        }
        return found;
    }

    public static void autoComplete(String name, String option, String[] options, CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals(name) && event.getFocusedOption().getName().equals(option)) return;
        List<String> list = Arrays.asList(options);
        List<Command.Choice> choices = list.stream()
                .filter(word -> word.toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase()))
                .map(word -> new Command.Choice(word, word))
                .limit(25)
                .collect(Collectors.toList());
        event.replyChoices(choices).queue();
    }

    public static void deleteIn(Message message, long delay, TimeUnit unit) {
        message.delete().queueAfter(delay, unit);
    }
}