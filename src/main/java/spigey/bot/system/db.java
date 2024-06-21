package spigey.bot.system;

import org.mapdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import static spigey.bot.DiscordBot.jda;

public class db {
    private static DB db;
    private static final Logger logger = LoggerFactory.getLogger(db.class);
    private static ConcurrentMap<String, String> dataMap;
    private static ConcurrentMap<String, HashSet<String>> postsMap;
    private static String def;

    public static void init(String DatabaseFile, String DefaultValue) {
        try {
            File dbFile = new File(DatabaseFile);
            dbFile.getParentFile().mkdirs();
            if (!dbFile.exists()) dbFile.createNewFile();
            db = DBMaker.fileDB(DatabaseFile).fileMmapEnable().make();
            def = DefaultValue;
            dataMap = db.hashMap("data", Serializer.STRING, Serializer.STRING).createOrOpen();
            postsMap = db.hashMap("posts", Serializer.STRING, Serializer.JAVA).createOrOpen();
            logger.info("Initialized MapDB Database");
        } catch (Exception e) {
            logger.error("Failed to initialize MapDB Database: {}", sys.getStackTrace(e));
        }
    }

    public static void write(String User, String Key, String Value) {
        try {
            dataMap.put(User + "_" + Key, Value);
            db.commit();
        } catch (Exception e) {
            logger.error("Failed to write to database with error: {}", sys.getStackTrace(e));
        }
    }

    public static String read(String User, String Key, String DefaultValue) {
        String value = dataMap.get(User + "_" + Key);
        return value != null ? value : DefaultValue;
    }

    public static String read(String User, String Key) {
        return read(User, Key, def);
    }

    public static void add(String User, String Key, int Value){
        try{
            write(User, Key, String.valueOf(Integer.parseInt(read(User, Key)) + Value));
            db.commit();
        } catch(Exception e){
            logger.error("Failed to add to database with error: {}", sys.getStackTrace(e));
        }
    }

    public static void remove(String User, String Key){
        try {
            dataMap.remove(User + "_" + Key);
            db.commit();
        } catch (Exception e) {
            logger.error("Failed to remove from database with error: {}", sys.getStackTrace(e));
        }
    }

    public static void remove(String User){
        try {
            dataMap.keySet().removeIf(key -> key.startsWith(User + "_"));
            db.commit();
        } catch (Exception e) {
            logger.error("Failed to remove from database with error: {}", sys.getStackTrace(e));
        }
    }

    public static JSONArray getArray(String Array) {
        JSONArray result = new JSONArray();
        try {
            for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                if (entry.getKey().startsWith(Array + "_")) {
                    JSONObject obj = new JSONObject();
                    obj.put(entry.getKey().substring(Array.length() + 1), entry.getValue());
                    result.add(obj);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get array from database with error: {}", sys.getStackTrace(e));
        }
        return result;
    }

    public static int keySize(){
        return dataMap.size();
    }

    public static String idFromToken(String Token){
        try{
            for(Map.Entry<String, String> entry : dataMap.entrySet()) {
                if (entry.getKey().endsWith("_token") && entry.getValue().equals(Token)) {
                    return entry.getKey().split("_")[0]; // Extract User from the composite key
                }
            }
        } catch(Exception e){
            logger.error("Failed to retrieve ID from Token from database with error: {}", sys.getStackTrace(e));
        }
        return null;
    }

    public static void postMeta(String PostID, String MessageID) {
        try {
            postsMap.compute(PostID, (key, value) -> {
                if (value == null) {
                    value = new HashSet<>();
                }
                value.add(MessageID);
                return value;
            });
            db.commit();
        } catch (Exception e) {
            logger.error("Failed to update Post Metadata from database with error: {}", sys.getStackTrace(e));
        }
    }

    public static void deletePost(String PostID) {
        try {
            if (!postsMap.containsKey(PostID)) {
                logger.warn("Post with ID {} not found in database", PostID);
                return;
            }
            Set<String> msgIDs = postsMap.get(PostID);
            if (msgIDs == null) return;
            for (String msgID : msgIDs) {
                String[] split = msgID.split("-");
                Message message = jda.getGuildById(split[0]).getTextChannelById(split[1]).retrieveMessageById(split[2]).complete();
                if (message == null) continue;
                message.editMessageEmbeds(new EmbedBuilder(message.getEmbeds().get(0))
                        .setDescription("*This post has been removed by a " + jda.getSelfUser().getName() + " moderator.*").build()).queue();
                message.delete().queue();
                message.editMessageComponents(Collections.emptyList()).queue();
            }
            postsMap.remove(PostID);
            db.commit();
        } catch (Exception e) {
            logger.error("Failed to delete Post from database with error: {}", sys.getStackTrace(e));
        }
    }

    public static String postID(String MessageID) {
        try {
            for (Map.Entry<String, HashSet<String>> entry : postsMap.entrySet()) {
                if (entry.getValue().contains(MessageID)) {
                    return entry.getKey();
                }
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve Post ID from database with error: {}", sys.getStackTrace(e));
        }
        return null;
    }
}

