package spigey.bot.system;

import org.iq80.leveldb.*;
import static org.fusesource.leveldbjni.JniDBFactory.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static spigey.bot.DiscordBot.jda;

public class db {
    private static DB db;
    private static final Logger logger = LoggerFactory.getLogger(db.class);
    private static String def;

    private static final JSONParser parser = new JSONParser();

    /**
     * Initializes the LevelDB database with the specified path and default value.
     * It sets up the database options and logs whether the initialization was successful.
     *
     * @param DatabasePath The path to the database file.
     * @param DefaultValue The default value to use when reading from the database.
     */
    public static void init(String DatabasePath, String DefaultValue) {
        try {
            Options options = new Options();
            options.createIfMissing(true);
            def = DefaultValue;
            db = factory.open(new File(DatabasePath), options);
            logger.info("Initialized LevelDB Database");
        } catch (Exception e) {
            logger.error("Failed to initialize LevelDB Database: {}", sys.getStackTrace(e));
        }
    }

    /**
     * Retrieves a JSON document from the database by its ID.
     * If the document does not exist, it returns an empty JSON object.
     *
     * @param id The ID of the document to retrieve.
     * @return The JSON object retrieved from the database.
     */
    private static JSONObject getDocument(String id) {
        try {
            byte[] valueBytes = db.get(bytes(id));
            return (valueBytes != null) ? (JSONObject) parser.parse(asString(valueBytes)) : new JSONObject();
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    /**
     * Saves a JSON document to the database with the specified ID.
     *
     * @param id The ID to associate with the document.
     * @param data The JSON data to save.
     */
    private static void saveDocument(String id, JSONObject data) {
        try {
            db.put(bytes(id), bytes(data.toJSONString()));
        } catch (Exception e) {
            logger.error("Failed to write to LevelDB: {}", sys.getStackTrace(e));
        }
    }

    /**
     * Writes a key-value pair to the database for a specified user.
     *
     * @param User The user ID.
     * @param Key The key to write.
     * @param Value The value to write.
     */
    public static void write(String User, String Key, String Value) {
        try {
            JSONObject document = getDocument(User);
            document.put(Key, Value);
            saveDocument(User, document);
        } catch (Exception e) {
            logger.error("Failed to write to database: {}", sys.getStackTrace(e));
        }
    }

    /**
     * Reads a value from the database for a specified user and key.
     * If the key does not exist, returns the specified default value.
     *
     * @param User The user ID.
     * @param Key The key to read.
     * @param DefaultValue The default value to return if the key does not exist.
     * @return The value associated with the key, or the default value if the key does not exist.
     */
    public static String read(String User, String Key, String DefaultValue) {
        try {
            JSONObject document = getDocument(User);
            if(document.containsKey(Key)) {
                return (String) document.get(Key);
            }
        } catch (Exception e) {
            logger.error("Failed to read from database with error: {}", sys.getStackTrace(e));
        }
        return DefaultValue;
    }

    /**
     * Reads a value from the database for a specified user and key.
     * Uses the default value specified during initialization if the key does not exist.
     *
     * @param User The user ID.
     * @param Key The key to read.
     * @return The value associated with the key, or the default value if the key does not exist.
     */
    public static String read(String User, String Key) {
        return read(User, Key, def);
    }

    /**
     * Adds a specified value to an existing value in the database for a specified user and key.
     * If the key does not exist, initializes it to zero before adding.
     *
     * @param User The user ID.
     * @param Key The key to add to.
     * @param Value The value to add.
     */
    public static void add(String User, String Key, int Value) {
        try {
            write(User, Key, String.valueOf(Integer.parseInt(read(User, Key, "0")) + Value));
        } catch (Exception e) {
            logger.error("Failed to add to database: {}", sys.getStackTrace(e));
        }
    }

    /**
     * Removes a specific key-value pair from the database for a specified user.
     *
     * @param User The user ID.
     * @param Key The key to remove.
     */
    public static void remove(String User, String Key) {
        try {
            db.delete(bytes(User + "_" + Key));
        } catch (Exception e) {
            logger.error("Failed to remove object from LevelDB: {}", sys.getStackTrace(e));
        }
    }

    /**
     * Removes all key-value pairs associated with a specified user from the database.
     *
     * @param User The user ID.
     */
    public static void remove(String User) {
        try (DBIterator iterator = db.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                if (key.startsWith(User + "_")) {
                    db.delete(iterator.peekNext().getKey());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to remove array from LevelDB: {}", sys.getStackTrace(e));
        }
    }

    /**
     * Retrieves an array of JSON objects from the database starting with a specified prefix.
     *
     * @param Array The prefix to search for.
     * @return A JSON array of objects retrieved from the database.
     */
    public static JSONArray getArray(String Array) {
        JSONArray result = new JSONArray();
        try (DBIterator iterator = db.iterator()) {
            iterator.seek(bytes(Array));
            while (iterator.hasNext()) {
                Map.Entry<byte[], byte[]> entry = iterator.next();
                String key = asString(entry.getKey());
                if (!key.startsWith(Array)) break;
                JSONObject value = (JSONObject) parser.parse(asString(entry.getValue()));
                for (Object keyObj : value.keySet()) {
                    String innerKey = (String) keyObj;
                    JSONObject obj = new JSONObject();
                    obj.put(innerKey, value.get(innerKey));
                    result.add(obj);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get array from database with error: {}", e.getMessage());
        }
        return result;
    }

    /**
     * Returns the number of keys in the database.
     *
     * @return The number of keys in the database.
     */
    public static int keySize() {
        int count = 0;
        try (DBIterator iterator = db.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); ) {
                iterator.next();
                count++;
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve key size from database with error: {}", sys.getStackTrace(e));
        }
        return count;
    }

    /**
     * Retrieves a user ID from the database using a token.
     *
     * @param Token The token to search for.
     * @return The user ID associated with the token, or null if not found.
     */
    public static String idFromToken(String Token) {
        try (DBIterator iterator = db.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); ) {
                Map.Entry<byte[], byte[]> entry = iterator.peekNext();
                String key = asString(entry.getKey());
                if (key.endsWith("_token") && asString(entry.getValue()).equals(Token)) {
                    return key.split("_")[0];
                }
                iterator.next();
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve ID from Token from database with error: {}", sys.getStackTrace(e));
        }
        return null;
    }

    /**
     * Updates post metadata in the database by adding a message ID to a specified post ID.
     *
     * @param postID The post ID.
     * @param messageID The message ID to add.
     */
    public static void postMeta(String postID, String messageID) {
        try {
            JSONObject post = getDocument("posts");
            JSONArray messageIDs = (JSONArray) post.getOrDefault(postID, new JSONArray());
            messageIDs.add(messageID);
            post.put(postID, messageIDs);
            saveDocument("posts", post);
        } catch (Exception e) {
            logger.error("Failed to update Post Metadata from database with error: {}", sys.getStackTrace(e));
        }
    }

    /**
     * Deletes a post and its associated messages from the database.
     *
     * @param postID The post ID to delete.
     */
    public static void deletePost(String postID) {
        try {
            JSONObject posts = getDocument("posts");
            if (posts.containsKey(postID)) {
                JSONArray msgIDs = (JSONArray) posts.get(postID);
                for (Object msgIDObj : msgIDs) {
                    String msgID = (String) msgIDObj;
                    String[] split = msgID.split("-");
                    Message message = jda.getGuildById(split[0]).getTextChannelById(split[1]).retrieveMessageById(split[2]).complete();
                    if (message != null) {
                        message.editMessageEmbeds(new EmbedBuilder(message.getEmbeds().get(0))
                                .setDescription("*This post has been removed by a " + jda.getSelfUser().getName() + " moderator.*").build()).queue();
                        message.delete().queue();
                        message.editMessageComponents(Collections.emptyList()).queue();
                    }
                }
                posts.remove(postID);
                saveDocument("posts", posts);
            } else {
                logger.warn("Post with ID {} not found in database", postID);
            }
        } catch (Exception e) {
            logger.error("Failed to delete Post from database with error: {}", sys.getStackTrace(e));
        }
    }

    /**
     * Retrieves a post ID from the database using a message ID.
     *
     * @param MessageID The message ID to search for.
     * @return The post ID associated with the message ID, or null if not found.
     */
    public static String postID(String MessageID){
        try {
            JSONObject posts = getDocument("posts");
            for (Object keyObj : posts.keySet()) {
                String key = (String) keyObj;
                JSONArray msgIDs = (JSONArray) posts.get(key);
                if (msgIDs.contains(MessageID)) {
                    return key;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve Post ID from database with error: {}", sys.getStackTrace(e));
        }
        return null;
    }
}