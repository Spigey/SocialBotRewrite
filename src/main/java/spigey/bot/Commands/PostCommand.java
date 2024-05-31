package spigey.bot.Commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import spigey.bot.system.*;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@CommandInfo(
        slashCommand = "post",
        cooldown = 300000
)
public class PostCommand implements Command {
    @Override
    public int slashCommand(SlashCommandInteractionEvent event) throws Exception {
        if(event.getGuild() == null){event.reply("Commands are disabled in DMs, tough luck mguy").queue(); return 0;}
        if(Objects.equals(db.read(event.getUser().getId(), "account"), "0")){event.reply("You have to register first to post something!").setEphemeral(true).queue(); return 0;}
        if(Objects.equals(db.read("channels", event.getGuild().getId()), "0")){event.reply("This bot hasn't been set up on this server yet! Tell an admin to run `/setchannel <channel>`.").queue(); return 0;}
        if(event.getOption("content").getAsString().length() > 300){event.reply("Your post must at most be 300 characters in length!").setEphemeral(true).queue(); return 0;}
        JSONArray channelsArray = db.getArray("channels");
        String username = db.read(event.getUser().getId(), "account");
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(String.format("New post by @%s %s", username, db.read("verified", username, "")))
                .setDescription(event.getOption("content").getAsString())
                .setColor(EmbedColor.RED)
                .setTimestamp(Instant.now());
        for (Object obj : channelsArray) {
            JSONObject guildChannels = (JSONObject) obj;
            for (Object guildId : guildChannels.keySet()) {
                String guild;
                String channel;
                try{guild = event.getJDA().getGuildById((String) guildId).getId();}catch(Exception L){db.remove("channels", guildId.toString());continue;}
                try{channel = guildChannels.get(guildId).toString();} catch(Exception L){db.remove("channels", guildId.toString());continue;}
                TextChannel post = event.getJDA().getGuildById(guild).getTextChannelById(channel);
                post.sendMessage("").addEmbeds(embed.build()).queue();
            }
        }
        AtomicReference<String> url = new AtomicReference<>("");
        event.getJDA().getGuildById("1246040271435730975").getTextChannelById("1246040631801810986").sendMessage("").addEmbeds(embed.build()).queue(message -> {
            url.set(message.getJumpUrl());
            try {
                db.write("posts", username, db.read("posts", username, "") + "\n[" + sys.trim(event.getOption("content").getAsString(),7) +"](" + url.get() + ")");
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
            event.reply("Successfully posted to " + channelsArray.size() + " servers!").setEphemeral(true).queue();
        });
        return 1;
    }
}
