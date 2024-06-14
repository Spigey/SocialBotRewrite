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

import static spigey.bot.DiscordBot.jda;
import static spigey.bot.DiscordBot.prefix;

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
        log("\u001B[42;30m[DEBUG]: " + content + " \u001B[49m");
        if(chat) msg("```\n" + content + "\n```");
    }
    public static void error(Object content, boolean chat) {
        log("\u001B[41;30m[ERROR]: " + content + "\u001B[0m");
        if(chat) msg("```\n" + content + "\n```");
    }
    public static void warn(Object content, boolean chat) {
        log("\u001B[43;30m[WARN]: " + content + "\u001B[0m");
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
    public static boolean notif(String username, MessageEmbed embed) throws Exception {
        boolean found = false;
        try {
            JSONObject existingData = (JSONObject) new JSONParser().parse(new FileReader("src/main/java/spigey/bot/system/database/database.json"));
            for (Object userId : existingData.keySet()) {
                JSONArray userData = (JSONArray) existingData.get(userId);
                for (Object obj : userData) {
                    JSONObject userObject = (JSONObject) obj;
                    if (userObject.containsKey("account") && userObject.get("account").equals(username)) {
                        User user = jda.retrieveUserById((String) userId).complete();
                        if (user != null) {
                            found = true;
                            user.openPrivateChannel().queue(privateChannel -> {
                                privateChannel.sendMessage("").addEmbeds(embed).queue();
                            });
                        }
                    }
                }
            }
        }catch(Exception L){
            sys.errInfo(L);
            return false;
        }
        return found;
    }

    public static boolean userExists(String username){
        return !Objects.equals(db.read(username, "password", ""), "");
    }

    public static void notif(String username, MessageEmbed embed, Button... buttons) throws Exception {
        JSONObject existingData = (JSONObject) new JSONParser().parse(new FileReader("src/main/java/spigey/bot/system/database/database.json"));
        for (Object userId : existingData.keySet()) {
            JSONArray userData = (JSONArray) existingData.get(userId);
            for (Object obj : userData) {
                JSONObject userObject = (JSONObject) obj;
                if (userObject.containsKey("account") && userObject.get("account").equals(username)) {
                    User user = jda.retrieveUserById((String) userId).complete();
                    if (user != null) {
                        user.openPrivateChannel().queue(privateChannel -> {
                            privateChannel.sendMessage("").addEmbeds(embed).addActionRow(buttons).queue();
                        });
                    }
                }
            }
        }
    }

    public static CompletableFuture<List<User>> userExecF(String username) {
        CompletableFuture<List<User>> future = new CompletableFuture<>();
        List<User> retrievedUsers = new ArrayList<>();
        List<CompletableFuture<User>> userFutures = new ArrayList<>();

        CompletableFuture.runAsync(() -> {
            try (FileReader reader = new FileReader("src/main/java/spigey/bot/system/database/database.json")) {
                JSONObject existingData = (JSONObject) new JSONParser().parse(reader);

                for (Object key : existingData.keySet()) {
                    String userId = (String) key;
                    JSONArray userData = (JSONArray) existingData.get(key);
                    for (Object obj : userData) {
                        JSONObject userObject = (JSONObject) obj;
                        if (userObject.containsKey("account") && (username.equals("*") || userObject.get("account").equals(username))) {
                            RestAction<User> userAction = jda.retrieveUserById(userId);
                            CompletableFuture<User> userFuture = new CompletableFuture<>();
                            userAction.queue(userFuture::complete, userFuture::completeExceptionally);
                            userFutures.add(userFuture);
                            break; // Assumption: Only one match per userId
                        }
                    }
                }

                CompletableFuture.allOf(userFutures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> {
                            for (CompletableFuture<User> userFuture : userFutures) {
                                try {
                                    retrievedUsers.add(userFuture.join());
                                } catch (Exception e) {
                                    // Handle individual exceptions if needed
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
        try (FileReader reader = new FileReader("src/main/java/spigey/bot/system/database/database.json")) {
            JSONObject existingData = (JSONObject) new JSONParser().parse(reader);

            if (existingData.containsKey(username)) {
                JSONArray userData = (JSONArray) existingData.get(username);
                for (Object obj : userData) {
                    if (obj instanceof JSONObject && ((JSONObject) obj).containsKey("account")) {
                        String userId = (String) ((JSONObject) obj).get("account");
                        jda.retrieveUserById(userId).queue(user -> {
                            if (user != null) {
                                action.accept(user);
                            }
                        });
                        return true;
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
                .limit(25) // Maximum of 25 choices allowed
                .collect(Collectors.toList());
        event.replyChoices(choices).queue();
    }

    public static void deleteIn(Message message, long delay, TimeUnit unit) {
        message.delete().queueAfter(delay, unit);
    }
}