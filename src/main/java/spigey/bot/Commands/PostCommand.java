package spigey.bot.Commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import spigey.bot.system.*;

import java.time.Instant;
import java.util.Objects;

import static spigey.bot.system.sys.debug;

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
        JSONArray channelsArray = db.getArray("channels");
        for (Object obj : channelsArray) {
            JSONObject guildChannels = (JSONObject) obj;
            for (Object guildId : guildChannels.keySet()) {
                String guild;
                String channel;
                try{guild = event.getJDA().getGuildById((String) guildId).getId();}catch(Exception L){db.remove("channels", guildId.toString());continue;}
                try{channel = guildChannels.get(guildId).toString();} catch(Exception L){db.remove("channels", guildId.toString());continue;}
                TextChannel post = event.getJDA().getGuildById(guild).getTextChannelById(channel);

                String username = db.read(event.getUser().getId(), "account");
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(String.format("New post by @%s %s", username, db.read("verified", username, "")))
                        .setDescription(event.getOption("content").getAsString())
                        .setColor(EmbedColor.RED)
                        .setTimestamp(Instant.now());
                post.sendMessage("").addEmbeds(embed.build()).queue();
            }
        }
        event.reply("Successfully posted!").setEphemeral(true).queue();
        return 1;
    }
}
