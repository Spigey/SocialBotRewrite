package spigey.bot.system;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

import static spigey.bot.system.util.log;

public class db {
    private static String defaultValue = "temp";
    private static final String FILE_PATH = "src/main/java/spigey/bot/system/database/database.json";

    public static void write(String user, String key, String value) throws IOException, ParseException {
        JSONObject existingData = (JSONObject) new JSONParser().parse(new FileReader(FILE_PATH));

        JSONArray userData = (JSONArray) existingData.getOrDefault(String.valueOf(user), new JSONArray());
        JSONObject userObjectToUpdate = null;
        for (Object o : userData) {
            if (((JSONObject) o).containsKey(key)) {
                userObjectToUpdate = (JSONObject) o;
                break;
            }
        }

        if (userObjectToUpdate != null) {userObjectToUpdate.put(key, value);} else { userData.add(new JSONObject() {{ put(key, value); }});}

        existingData.put(String.valueOf(user), userData);

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
    public static void setDefaultValue(String val){
        defaultValue = val;
    }
    public static String getDefaultValue(){
        return defaultValue;
    }
    public static void add(String user, String key, int value) throws IOException, ParseException {
        if(Objects.equals(read(user, key), getDefaultValue())){
            log("Adding key " + key + " to user " + user);
            write(user, key, String.valueOf(value));
            return;
        }
        int existingValue = (read(user, key) != null) ? Integer.parseInt(read(user, key)) : 0;
        write(user, key, String.valueOf(existingValue + value));
    }
}
