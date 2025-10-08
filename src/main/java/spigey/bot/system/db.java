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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static spigey.bot.DiscordBot.jda;

@SuppressWarnings("all")
public class db {
    private static DB db;
    private static final Logger logger = LoggerFactory.getLogger(db.class);
    private static String def;
    private static String path;

    private static final JSONParser parser = new JSONParser();

    /**
     * Initializes the db database with the specified path and default value.
     * It sets up the database options and logs whether the initialization was successful.
     *
     * @param DatabasePath The path to the database file.
     * @param DefaultValue The default value to use when an element was not found while reading from the database.
     */
    public static void init(String DatabasePath, String DefaultValue) {
        try {
            path = DatabasePath;
            Options options = new Options();
            options.createIfMissing(true);
            def = DefaultValue;
            db = factory.open(new File(DatabasePath), options);
            logger.info("Initialized db Database at '{}' ({} keys, {})", DatabasePath, keySize(), fileSize("%.1f %s"));
        } catch (Exception e) {
            logger.error("Failed to initialize db Database: {}", sys.getStackTrace(e));
        }
    }

    /**
     * Retrieves a JSON document from the database by its ID.
     * If the document does not exist, it returns an empty JSON object.
     * You will rarely need this.
     *
     * @param id The ID of the document to retrieve.
     * @return The JSON object retrieved from the database.
     */
    public static JSONObject getDocument(String id) {
        try {
            byte[] valueBytes = db.get(bytes(id));
            return (valueBytes != null) ? (JSONObject) parser.parse(asString(valueBytes)) : new JSONObject();
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    /**
     * Saves a JSON document to the database with the specified ID.
     * You will rarely need this.
     *
     * @param id The ID to associate with the document.
     * @param data The JSON data to save.
     */
    public static void saveDocument(String id, JSONObject data) {
        try {
            db.put(bytes(id), bytes(data.toJSONString()));
        } catch (Exception e) {
            logger.error("Failed to write to db: {}", sys.getStackTrace(e));
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
            if(document.containsKey(Key)) return document.get(Key).toString();
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
            JSONObject doc = getDocument(User);
            doc.remove(Key);
            saveDocument(User, doc);
        } catch (Exception e) {
            logger.error("Failed to remove object from db: {}", sys.getStackTrace(e));
        }
    }

    /**
     * Removes all key-value pairs associated with a specified user from the database.
     *
     * @param User The user ID.
     */
    public static void remove(String User) {
        try (WriteBatch batch = db.createWriteBatch()) {
            DBIterator iterator = db.iterator();
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                if (key.equals(User)) batch.delete(iterator.peekNext().getKey());
            }
            db.write(batch);
        } catch (Exception e) {
            logger.error("Failed to remove array from db: {}", sys.getStackTrace(e));
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
     * Returns the file size of the database in a human-readable format.
     *
     * @param Format The format of which the File size should be returned, (e.g., "%.1f%s" -> "34KB")
     * @return The file size as a string (e.g., "2.5 MB", "1.2 GB"), or "ERR" on error.
     */
    public static String fileSize(String Format) { // %.1f%s
        try {
            long size = Files.size(Paths.get(path));
            String unit = size < 1024 ? "B" : size < 1024 * 1024 ? "KB" : size < 1024 * 1024 * 1024 ? "MB" : "GB";
            size = (size >= 1024) ? size / 1024 : size;
            size = (size >= 1024) ? size / 1024 : size;
            size = (size >= 1024) ? size / 1024 : size;
            return String.format(Format, (double) size, unit);
        } catch (Exception e) {
            logger.error("Failed to get file size from database: {}", e.getMessage());
            return "ERR";
        }
    }

    /**
     * Returns the file size of the database in a human-readable format.
     *
     * @return The file size as a string (e.g., "2.5 MB", "1.2 GB"), or "ERR" on error.
     */
    public static String fileSize() { // %.1f%s
        return fileSize("%.1f %s");
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
                    Message message = jda.getGuildById(split[0]).getTextChannelById(read("channels", split[0])).retrieveMessageById(split[1]).complete();
                    message.editMessageEmbeds(new EmbedBuilder(message.getEmbeds().getFirst())
                            .setDescription("*This post has been removed by a " + jda.getSelfUser().getName() + " moderator.*")
                            .setImage(null).build()).queue();
                    message.editMessageComponents(Collections.emptyList()).queue();
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
            for (Object key : posts.keySet()) {
                System.out.println(posts.get(key));
                for(Object id : ((JSONArray) posts.get(key))){
                    if(id.toString().split("-")[1].equals(MessageID)) return key.toString();
                }
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve Post ID from database with error: {}", sys.getStackTrace(e));
        }
        return null;
    }

    public static DB retrieve() {
        return db;
    }
}