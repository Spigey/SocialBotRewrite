package spigey.bot.system;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static spigey.bot.system.util.log;

public class db {
    private static String defaultValue = "temp";
    private static final String FILE_PATH = "src/main/java/spigey/bot/system/database/database.json";

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
}
