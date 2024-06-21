package spigey.bot.system;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import spigey.bot.DiscordBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static spigey.bot.DiscordBot.jda;

public class db {
    private static Firestore db;
    private static final Logger logger = LoggerFactory.getLogger(db.class);
    private static final Map<String, String> config = Map.of(
            "Collection", "data",
            "DefaultValue", "0"
    );

    /**
     * Initializes the Firestore Database.
     *
     * @param ProjectID The project ID of the Firestore project.
     * @throws IOException If there's an error initializing the database.
     */
    public static void init(String ProjectID) throws IOException {
        InputStream serviceAccount = db.class.getClassLoader().getResourceAsStream("account.json");
        db = FirestoreOptions.getDefaultInstance().toBuilder()
                .setProjectId(ProjectID)
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build().getService();
        logger.info("Initialized Firestore Database");
    }

    /**
     * Writes a key-value pair to a Firestore document.
     *
     * @param User   The document ID (user identifier).
     * @param Key    The key to store the value under.
     * @param Value  The value to store.
     */
    public static void write(String User, String Key, String Value){
        try{
            db.collection(config.get("Collection")).document(User).update(Key, Value).get();
        }catch (Exception e){
            logger.error("Failed to write to Firestore with error: {}", sys.getStackTrace(e));
        }
    }

    /**
     * Reads a value from a Firestore document, with a default value if not found.
     *
     * @param User         The document ID (user identifier).
     * @param Key          The key to retrieve the value from.
     * @param DefaultValue The default value to return if the key is not found or an error occurs.
     * @return The value associated with the key, or the default value if not found.
     */
    public static String read(String User, String Key, String DefaultValue){
        try{
            DocumentSnapshot document = db.collection(config.get("Collection")).document(User).get().get();
            if(document.exists() && document.contains(Key)) return document.getString(Key);
        } catch (Exception e) {
            logger.error("Failed to read from Firestore with error: {}", sys.getStackTrace(e));
        }
        return DefaultValue;
    }

    /**
     * Reads a value from a Firestore document.
     *
     * @param User The document ID (user identifier).
     * @param Key  The key to retrieve the value from.
     * @return The value associated with the key, or the default value if not found.
     */
    public static String read(String User, String Key){
        return read(User, Key, config.get("DefaultValue"));
    }

    /**
     * Adds a numeric value to an existing key in a Firestore document, or creates the key with the value if it doesn't exist.
     *
     * @param User  The document ID (user identifier).
     * @param Key   The key to add the value to.
     * @param Value The numeric value to add.
     */
    public static void add(String User, String Key, Object Value){
        try{
            DocumentReference doc = db.collection(config.get("Collection")).document(User);
            int finalValue = Integer.parseInt(Value.toString());
            db.runTransaction(transaction -> {
                DocumentSnapshot snapshot = transaction.get(doc).get();
                transaction.update(doc, Key, snapshot.contains(Key) ? snapshot.getLong(Key).intValue() + finalValue : finalValue);
                return null;
            }).get();
        } catch(Exception e){
            logger.error("Failed to add to Firestore with error: {}", sys.getStackTrace(e));
        }
    }

    /**
     * Removes a specific key-value pair from a Firestore document.
     *
     * @param User The document ID (user identifier).
     * @param Key  The key to remove.
     */
    public static void remove(String User, String Key){
        HashMap <String, Object> updates = new HashMap<>();
        updates.put(Key, FieldValue.delete());
        db.collection(config.get("Collection")).document(User).update(updates);
    }

    /**
     * Removes an entire document from the Firestore collection.
     *
     * @param User The document ID (user identifier) to remove.
     */
    public static void remove(String User){
        db.collection(config.get("Collection")).document(User).delete();
    }

    /**
     * Retrieves an array from a Firestore document and converts it to a JSONArray.
     *
     * @param Array The ID of the document containing the array.
     * @return A JSONArray representing the data in the document, or an empty JSONArray if not found or an error occurs.
     */
    public static JSONArray getArray(String Array) {
        JSONArray result = new JSONArray();
        try {
            DocumentSnapshot document = db.collection(config.get("Collection")).document(Array).get().get();
            if (document.exists()) {
                for (Map.Entry<String, Object> entry : document.getData().entrySet()) {
                    JSONObject obj = new JSONObject();
                    obj.put(entry.getKey(), entry.getValue());
                    result.add(obj);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get array from Firestore with error: {}", sys.getStackTrace(e));
        }
        return result;
    }

    /**
     * Calculates the total number of keys across all documents in the collection.
     *
     * @return The total number of keys.
     */
    public static int keySize(){
        int count = 0;
        try{
            for(DocumentReference doc : db.collection(config.get("Collection")).listDocuments()){
                DocumentSnapshot document = doc.get().get();
                if(!document.exists()) continue;
                count += document.getData().size();
            }
        }catch (Exception e){
            logger.error("Failed to retrieve key size from Firestore with error: {}", sys.getStackTrace(e));
        }
        return count;
    }

    /**
     * Retrieves the user ID associated with a given token.
     *
     * @param Token The token to search for.
     * @return The user ID associated with the token, or null if not found.
     */
    public static String idFromToken(String Token){
        try{
            for(DocumentReference doc : db.collection(config.get("Collection")).listDocuments()){
                DocumentSnapshot document = doc.get().get();
                if(!(document.exists() && document.contains("token") && document.getString("token").equals(Token))) continue;
                return document.getId();
            }
        } catch(Exception e){
            logger.error("Failed to retrieve ID from Token from Firestore with error: {}", sys.getStackTrace(e));
        }
        return null;
    }

    /**
     * Stores post metadata for a specific post ID, including the associated message IDs.
     *
     * @param PostID     The unique identifier for the post.
     * @param MessageID  The unique identifier for the message associated with the post.
     */
    public static void postMeta(String PostID, String MessageID){
        try{
            DocumentReference doc = db.collection(config.get("Collection")).document("posts");
            db.runTransaction(transaction -> {
                DocumentSnapshot snapshot = transaction.get(doc).get();

                Map<String, Object> data = snapshot.exists() ? snapshot.getData() : new HashMap<>();
                List<String> msgIDs = (List<String>) data.getOrDefault(PostID, new ArrayList<>());
                msgIDs.add(MessageID);
                data.put(PostID, msgIDs);
                transaction.set(doc, data, SetOptions.merge());
                return null;
            }).get();
        } catch(Exception e){
            logger.error("Failed to update Post Metadata from Firestore with error: {}", sys.getStackTrace(e));
        }
    }

    /**
     * Deletes a post with the given PostID from Firestore and updates the associated Discord messages.
     *
     * @param PostID The unique identifier for the post to delete.
     */
    public static void deletePost(String PostID){
        try {
            DocumentReference doc = db.collection(config.get("Collection")).document("posts");
            DocumentSnapshot document = doc.get().get();
            if(!document.exists() || !document.contains(PostID)){
                logger.warn("Post with ID {} not found in Firestore", PostID);
                return;
            }
            List<String> msgIDs = (List<String>) document.getData().get(PostID);
            if(msgIDs == null) return;
            for(String msgID : msgIDs){
                String[] split = msgID.split("-");
                Message message = jda.getGuildById(split[0]).getTextChannelById(split[1]).retrieveMessageById(split[2]).complete();
                if(message == null) continue;
                message.editMessageEmbeds(new EmbedBuilder(message.getEmbeds().get(0))
                        .setDescription("*This post has been removed by a " + jda.getSelfUser().getName() + " moderator.*").build()).queue();
                message.delete().queue();
                message.editMessageComponents(Collections.emptyList()).queue();
            }
            doc.update(PostID, FieldValue.delete()).get();
        } catch(Exception e){
            logger.error("Failed to delete Post from Firestore with error: {}", sys.getStackTrace(e));
        }
    }

    /**
     * Retrieves the PostID associated with a given MessageID.
     *
     * @param MessageID The unique identifier of the message.
     * @return The PostID associated with the message, or null if not found.
     */
    public static String postID(String MessageID){
        try {
            DocumentSnapshot document = db.collection(config.get("Collection")).document("posts").get().get();
            if (!document.exists()) return null;
            for (Map.Entry<String, Object> entry : document.getData().entrySet()) {
                if (!(entry.getValue() instanceof List<?> msgIDs && msgIDs.contains(MessageID))) continue;
                return entry.getKey();
            }
        } catch(Exception e){
            logger.error("Failed to retrieve Post ID from Firestore with error: {}", sys.getStackTrace(e));
        }
        return null;
    }
}
