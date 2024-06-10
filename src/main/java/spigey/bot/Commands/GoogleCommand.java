package spigey.bot.Commands;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import spigey.bot.system.Command;
import spigey.bot.system.CommandInfo;
import spigey.bot.system.sys;
import spigey.bot.system.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static spigey.bot.system.sys.choice;
import static spigey.bot.system.sys.sendApiRequest;

public class GoogleCommand implements Command {
    @Override
    public int slashCommand(SlashCommandInteractionEvent event) throws Exception {
        event.deferReply().queue();
        String response = "No response from API.";
        try{response = new Gson().fromJson(sendApiRequest("https://api.kastg.xyz/api/ai/blackbox?key=" + choice(new String[]{"Kastg_mwNnTJZK4KJ9XeVCBje4_free", "Kastg_VRfWQeIgMJmRZo5Wfx4D_free"}) + "&web_search=true&prompt=" + URLEncoder.encode(event.getOption("text").getAsString(), StandardCharsets.UTF_8), "GET", null, null), JsonObject.class).getAsJsonArray("result").get(0).getAsJsonObject().get("response").getAsString();}catch(Exception L){sys.errInfo(L);}
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Google")
                .setDescription(sys.trim(response, 4096));
        event.getHook().sendMessage("").addEmbeds(embed.build()).queue();
        return 1;
    }
}