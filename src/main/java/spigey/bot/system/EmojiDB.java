package spigey.bot.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EmojiDB {
    public static final String Cool = "<:cool:1245275314519543948>";
    public static final String Gold = "<:Gold:1094940563075252308>";
    public static final String WoodenPickaxe = "<:wooden_pickaxe:1040752631016067147>";
    public static final String StonePickaxe = "<:stone_pickaxe:1040752832787259472>";
    public static final String IronPickaxe = "<:iron_pickaxe:1040752894779068486>";
    public static final String GoldPickaxe = "<:gold_pickaxe:1052286936548659301>";
    public static final String DiamondPickaxe = "<:diamond_pickaxe:1040753073540321390>";
    public static final String NetheritePickaxe = "<:netherite_pickaxe:1040914413688979506>";
    public static final String DevPickaxe = "<:dev_pickaxe:1048297305968742400>";
    public static final String PremiumPickaxe = "<:PremiumPickaxe:1065594936025698434>";
    public static final String ApologiesPickaxe = "<:apologies_pickaxe:1050874021966774353>";
    public static final String NoPickaxe = "<:no_pickaxe:1040753304738742352> ";
    public static final String Verified = "<:verified:1245466839816077542>"; //
    public static final String Banned = "<:banned:1248397785619763222>";
    public static final String Credit = "<:MinecrafterCredit:1076436905157197834>";
    private static Map<String, String> emojis = new ConcurrentHashMap<>(1);
    public static void put(String name, String emoji){
        emojis.put(name, emoji);
    }
    public static String get(String emoji){
        return emojis.get(emoji);
    }
}
