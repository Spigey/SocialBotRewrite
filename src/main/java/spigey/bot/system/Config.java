package spigey.bot.system;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
    private JSONObject config;
    private final Logger logger = LoggerFactory.getLogger(Config.class);

    public Config(String Config) {
        try {
            this.config = (JSONObject) new JSONParser().parse(Config);
        } catch(ParseException e){
            logger.error("Malformed config string!");
            throw new RuntimeException(e);
        }
    }

    public Object get(String Option) {
        if(!config.containsKey(Option)){ logger.warn("Option {} not found in config!", Option); return null; }
        return config.get(Option);
    }
}