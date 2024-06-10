package spigey.bot.system;

import net.dv8tion.jda.api.EmbedBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import spigey.bot.DiscordBot;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static spigey.bot.DiscordBot.jda;
import static spigey.bot.system.util.log;

public class db {
    private static String defaultValue = "temp";
    public static final String FILE_PATH = Objects.equals(DiscordBot.config.get("DATABASE").toString(), "MAIN") ? DiscordBot.config.get("DATABASE_MAIN").toString() : DiscordBot.config.get("DATABASE_TEST").toString();

    public static void write(String user, String key, String value) throws IOException, ParseException {
        JSONObject existingData = (JSONObject) new JSONParser().parse(new FileReader(FILE_PATH));

        JSONArray userData = (JSONArray) existingData.getOrDefault(user, new JSONArray());
        JSONObject userObjectToUpdate = null;
        for (Object o : userData) {
            if (((JSONObject) o).containsKey(key)) {
                userObjectToUpdate = (JSONObject) o;
                break;
            }
        }

        if (userObjectToUpdate != null) {
            userObjectToUpdate.put(key, value);
        } else {
            userData.add(new JSONObject() {{ put(key, value); }});
        }

        existingData.put(user, userData);

        try (FileWriter database = new FileWriter(FILE_PATH)) {
            database.write(existingData.toJSONString());
        }
    }

    public static String read(String user, String key) throws IOException, ParseException {
        JSONObject existingData = (JSONObject) new JSONParser().parse(new FileReader(FILE_PATH));
        JSONArray userData = (JSONArray) existingData.getOrDefault(user, new JSONArray());
        for (Object obj : userData) {
            JSONObject userObject = (JSONObject) obj;
            if (userObject.containsKey(key)) {
                return (String) userObject.get(key);
            }
        }
        return defaultValue;
    }

    public static String read(String user, String key, String def) {
        try {
            JSONObject existingData = (JSONObject) new JSONParser().parse(new FileReader(FILE_PATH));
            JSONArray userData = (JSONArray) existingData.getOrDefault(user, new JSONArray());
            for (Object obj : userData) {
                JSONObject userObject = (JSONObject) obj;
                if (userObject.containsKey(key)) {
                    return (String) userObject.get(key);
                }
            }
        }catch (Exception L){/**/}
        return def;
    }

    public static void setDefaultValue(String val) {
        defaultValue = val;
    }

    public static String getDefaultValue() {
        return defaultValue;
    }

    public static void add(String user, String key, int value){
        try {
            if (Objects.equals(read(user, key), getDefaultValue())) {
                log("Adding key " + key + " to user " + user);
                write(user, key, String.valueOf(value));
                return;
            }
            int existingValue = (read(user, key) != null) ? Integer.parseInt(read(user, key)) : 0;
            write(user, key, String.valueOf(existingValue + value));
        }catch (Exception L){sys.errInfo(L);}
    }

    public static JSONArray getArray(String array) throws IOException, ParseException {
        JSONObject existingData = (JSONObject) new JSONParser().parse(new FileReader(FILE_PATH));
        return (JSONArray) existingData.get(array);
    }

    public static void remove(String user, String key){
        try {
            JSONObject existingData = (JSONObject) new JSONParser().parse(new FileReader(FILE_PATH));

            JSONArray userData = (JSONArray) existingData.getOrDefault(user, new JSONArray());
            JSONObject userObjectToUpdate = null;
            for (Object o : userData) {
                if (((JSONObject) o).containsKey(key)) {
                    userObjectToUpdate = (JSONObject) o;
                    break;
                }
            }

            if (userObjectToUpdate != null) {
                userObjectToUpdate.remove(key);
                if (userObjectToUpdate.isEmpty()) {
                    userData.remove(userObjectToUpdate);
                }
            } else {
                // Key not found, nothing to remove
                return;
            }

            if (userData.isEmpty()) {
                existingData.remove(user);
            } else {
                existingData.put(user, userData);
            }

            try (FileWriter database = new FileWriter(FILE_PATH)) {
                database.write(existingData.toJSONString());
            }
        } catch(Exception L){
            sys.errInfo(L);
        }
    }
    public static void remove(String user){
        try {
            JSONObject existingData = (JSONObject) new JSONParser().parse(new FileReader(FILE_PATH));
            if (existingData.containsKey(user)) {
                existingData.remove(user);

                try (FileWriter database = new FileWriter(FILE_PATH)) {
                    database.write(existingData.toJSONString());
                }
            }
        }catch(Exception L){/**/}
    }

    public static String get() throws Exception {
        return Files.readString(Paths.get(FILE_PATH));
    }

    public static String idFromToken(String token) throws IOException, ParseException {
        JSONObject existingData = (JSONObject) new JSONParser().parse(new FileReader(FILE_PATH));
        for (Object userIdObj : existingData.keySet()) {
            String userId = (String) userIdObj;
            JSONArray userData = (JSONArray) existingData.get(userId);
            for (Object obj : userData) {
                JSONObject userObject = (JSONObject) obj;
                if (userObject.containsKey("token") && userObject.get("token").equals(token)) {
                    return userId;
                }
            }
        }
        return null;
    }

    public static int keySize() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject existingData = (JSONObject) parser.parse(new FileReader(FILE_PATH));
        return countKeysRecursive(existingData);
    }

    private static int countKeysRecursive(JSONObject obj) {
        int count = obj.size();  // Count keys at the current level

        for (Object value : obj.values()) {
            if (value instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) value;
                for (Object item : jsonArray) {
                    if (item instanceof JSONObject) {
                        count += countKeysRecursive((JSONObject) item);  // Recurse into objects within the array
                    }
                }
            } else if (value instanceof JSONObject) {
                count += countKeysRecursive((JSONObject) value); // Recurse into nested objects
            }
        }
        return count;
    }
    public static int clean() {
        try {
            JSONObject existingData = (JSONObject) new JSONParser().parse(new FileReader(FILE_PATH));
            int rem = cleanJSONObject(existingData); // No initial rm needed

            try (FileWriter database = new FileWriter(FILE_PATH)) {
                database.write(existingData.toJSONString());
            }

            return rem;
        } catch (Exception L) {
            sys.errInfo(L);
            return 0; // Return 0 in case of error
        }
    }

    private static int cleanJSONObject(JSONObject obj) {
        int removedCount = 0;

        for (Iterator<String> iterator = obj.keySet().iterator(); iterator.hasNext();) {
            String key = iterator.next();
            Object value = obj.get(key);

            if (value instanceof String) {
                String strValue = (String) value;
                if (strValue.trim().isEmpty()) {
                    iterator.remove();
                    removedCount++; // Increment counter for removed empty string
                }
            } else if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                for (Iterator<Object> arrayIterator = array.iterator(); arrayIterator.hasNext();) {
                    Object elem = arrayIterator.next();
                    if (elem instanceof JSONObject) {
                        removedCount += cleanJSONObject((JSONObject) elem); // Accumulate count from nested object
                        if (((JSONObject) elem).isEmpty()) {
                            arrayIterator.remove();
                            removedCount++; // Increment counter for removed nested empty object
                        }
                    } else if (elem instanceof JSONArray && ((JSONArray) elem).isEmpty()) {
                        arrayIterator.remove();
                        removedCount++; // Increment counter for removed nested empty array
                    }
                }
                if (array.isEmpty()) {
                    iterator.remove();
                    removedCount++; // Increment counter for removed empty array
                }
            } else if (value instanceof JSONObject) {
                removedCount += cleanJSONObject((JSONObject) value); // Accumulate count from nested object
                if (((JSONObject) value).isEmpty()) {
                    iterator.remove();
                    removedCount++; // Increment counter for removed nested empty object
                }
            }
        }

        return removedCount; // Return the count of removed elements
    }

    public static void postMeta(String postID, String messageId) throws Exception {
        JSONObject existingData = (JSONObject) new JSONParser().parse(new FileReader(FILE_PATH));
        JSONObject postsData = (JSONObject) existingData.getOrDefault("posts", new JSONObject());

        JSONObject postInfo = (JSONObject) postsData.getOrDefault(postID, new JSONObject());
        JSONArray messageIds = (JSONArray) postInfo.getOrDefault("messageIds", new JSONArray());
        messageIds.add(messageId);
        postInfo.put("messageIds", messageIds);

        postsData.put(postID, postInfo);
        existingData.put("posts", postsData);

        try (FileWriter database = new FileWriter(FILE_PATH)) {
            database.write(existingData.toJSONString());
        }
    }

    public static void deletePost(String postId) throws IOException, ParseException {
        JSONObject existingData = (JSONObject) new JSONParser().parse(new FileReader(FILE_PATH));
        JSONObject postsData = (JSONObject) existingData.getOrDefault("posts", new JSONObject());

        if (postsData.containsKey(postId)) {
            JSONObject postInfo = (JSONObject) postsData.get(postId);
            JSONArray messageIds = (JSONArray) postInfo.get("messageIds");

            for (Object messageIdObj : messageIds) {
                String[] split = ((String) messageIdObj).split("-");
                jda.getGuildById(split[0]).getTextChannelById(db.read("channels", split[0])).retrieveMessageById(split[1]).complete().editMessageEmbeds(new EmbedBuilder(jda.getGuildById(split[0]).getTextChannelById(db.read("channels", split[0])).retrieveMessageById(split[1]).complete().getEmbeds().get(0)).setDescription("*This post has been removed by a " + jda.getSelfUser().getName() + " moderator.*").build()).queue();
                jda.getGuildById(split[0]).getTextChannelById(db.read("channels", split[0])).retrieveMessageById(split[1]).complete().editMessageComponents(Collections.emptyList()).queue();
            }

            postsData.remove(postId);
            existingData.put("posts", postsData);

            try (FileWriter database = new FileWriter(FILE_PATH)) {
                database.write(existingData.toJSONString());
            }
        }
    }

    public static String postId(String messageId) throws IOException, ParseException {
        JSONObject postsData = (JSONObject) ((JSONObject) new JSONParser().parse(new FileReader(FILE_PATH))).getOrDefault("posts", new JSONObject());
        for (Object Id : postsData.keySet())
            if (((JSONArray) ((JSONObject) postsData.get(Id)).get("messageIds")).stream().anyMatch(msgId -> ((String) msgId).split("-")[1].equals(messageId))) return (String) Id;
        return null;
    }
}