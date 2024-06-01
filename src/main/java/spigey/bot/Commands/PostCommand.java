package spigey.bot.Commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import spigey.bot.system.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if(Objects.equals(db.read("verified", db.read(event.getUser().getId(), "account")), "0")) if(event.getOption("attachment") != null && !event.getOption("attachment").getAsAttachment().getContentType().contains("image/")){sys.warn(event.getUser().getId() + " attempted to upload " + event.getOption("attachment").getAsAttachment().getContentType()); event.reply("You are not allowed to post this file type!").setEphemeral(true).queue(); return 0;}
        JSONArray channelsArray = db.getArray("channels");
        String username = db.read(event.getUser().getId(), "account");
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(String.format("New post by @%s %s", username, db.read("verified", username, "")))
                .setDescription(txt(event.getOption("content").getAsString()))
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
                if(event.getOption("attachment") != null){
                    Path temp = Files.createTempFile(null, null);
                    event.getOption("attachment").getAsAttachment().downloadToFile(temp.toFile()).thenAccept(file -> {
                        File attachment = new File(file.getParent(), event.getOption("attachment").getAsAttachment().getFileName());
                        file.renameTo(attachment);
                        if(!event.getOption("attachment").getAsAttachment().getContentType().contains("image")){post.sendMessage("").addEmbeds(embed.build()).addFiles(FileUpload.fromData(attachment)).queue();}
                        else{
                            try {
                                EmbedBuilder img = new EmbedBuilder()
                                        .setTitle(String.format("New post by @%s %s", username, db.read("verified", username, "")))
                                        .setDescription(txt(event.getOption("content").getAsString()))
                                        .setColor(EmbedColor.RED)
                                        .setImage(event.getOption("attachment").getAsAttachment().getProxyUrl())
                                        .setTimestamp(Instant.now());
                                post.sendMessage("").addEmbeds(img.build()).queue();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                        file.delete();
                    });
                    continue;
                }
                post.sendMessage("").addEmbeds(embed.build()).queue();
            }
        }
        Pattern mentionPattern = Pattern.compile("@(\\w+)");
        Matcher matcher = mentionPattern.matcher(event.getOption("content").getAsString());
        Set<String> mentionedUsernames = new HashSet<>();
        while (matcher.find()) mentionedUsernames.add(matcher.group(1));
        for (String mentionedUsername : mentionedUsernames) {
            EmbedBuilder pingEmbed = new EmbedBuilder()
                    .setTitle("New Mention!")
                    .setDescription("**You were mentioned in a post by " + username + ":**\n" + txt(event.getOption("content").getAsString()))
                    .setColor(EmbedColor.GOLD);
            util.notif(mentionedUsername, pingEmbed.build());
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
        if(!Objects.equals(db.read("verified", username), "0")) return 0;
        return 1;
    }
    private static String txt(String text) throws IOException, ParseException {
        Pattern mentionPattern = Pattern.compile("@(\\w+)");
        Matcher matcher = mentionPattern.matcher(text);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            String username = matcher.group(1); // Extract the username
            String mention = "[@" + username + "]" + "(http://.)"; // Construct the mention format
            if(!Objects.equals(db.read("passwords", "password_" + username), "0")) matcher.appendReplacement(output, Matcher.quoteReplacement(mention));
        }
        matcher.appendTail(output);
        return output.toString();
    }
}