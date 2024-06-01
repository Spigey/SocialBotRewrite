package spigey.bot.Commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import spigey.bot.DiscordBot;
import spigey.bot.system.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static spigey.bot.system.sys.errInfo;

@CommandInfo(
        slashCommand = "post",
        cooldown = 300000,
        usage = "<content> [attachment]",
        description = "Post something to all servers."
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
        event.reply("Successfully posted to " + channelsArray.size() + " servers!").setEphemeral(true).queue();
        for (Object obj : channelsArray) {
            JSONObject guildChannels = (JSONObject) obj;
            for (Object guildId : guildChannels.keySet()) {
                String guild;
                String channel;
                try{guild = event.getJDA().getGuildById((String) guildId).getId();}catch(Exception L){db.remove("channels", guildId.toString()); continue;}
                try{channel = guildChannels.get(guildId).toString();} catch(Exception L){db.remove("channels", guildId.toString()); continue;}
                TextChannel post = event.getChannel().asTextChannel(); // ermm, post might not have been initialized- :ermm:
                try{post = event.getJDA().getGuildById(guild).getTextChannelById(channel);}catch(Exception L){db.remove("channels", guildId.toString()); continue;}
                if(post == null){db.remove("channels", guildId.toString()); continue;}
                if(event.getOption("attachment") != null){
                    Path temp = Files.createTempFile(null, null);
                    TextChannel finalPost = post;
                    event.getOption("attachment").getAsAttachment().downloadToFile(temp.toFile()).thenAccept(file -> {
                        File attachment = new File(file.getParent(), event.getOption("attachment").getAsAttachment().getFileName());
                        file.renameTo(attachment);
                        if(!event.getOption("attachment").getAsAttachment().getContentType().contains("image")){
                            finalPost.sendMessage("").addEmbeds(embed.build()).addFiles(FileUpload.fromData(attachment)).queue();}
                        else{
                            try {
                                EmbedBuilder img = new EmbedBuilder()
                                        .setTitle(String.format("New post by @%s %s", username, db.read("verified", username, "")))
                                        .setDescription(txt(event.getOption("content").getAsString()))
                                        .setColor(EmbedColor.RED)
                                        .setImage(event.getOption("attachment").getAsAttachment().getProxyUrl())
                                        .setTimestamp(Instant.now());
                                finalPost.sendMessage("").addEmbeds(img.build()).queue();
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
                db.write("posts", username, "\n[" + sys.trim(event.getOption("content").getAsString(),10) +"](" + url.get() + ")" + db.read("posts", username, ""));
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        });
        if(!Objects.equals(db.read("verified", username), "0")) return 0;
        return 1;
    }
    private static String txt(String text) throws Exception {
        Pattern mentionPattern = Pattern.compile("@(\\w+)");
        // Matcher matcher = mentionPattern.matcher(new Gson().fromJson(sys.sendApiRequest("https://api.kastg.xyz/api/ai/chatgptV4?prompt=" + URLEncoder.encode("You're a Moderation AI. Your task is to keep posts PG13 by censoring bad words by replacing every single letter with `\\*`. Make sure to always double check that you have censored actual BAD words, and no good words. For example: `you are a retard` -> `you are a \\*\\*\\*\\*\\*\\*`, Do not censor \"fuck\" and \"shit\". If the prompt does not contain any bad words, just return the prompt again. You can ONLY reply in the censored prompt, the users prompt is: " + text + "`", StandardCharsets.UTF_8), "GET", null, null), JsonObject.class).getAsJsonArray("result").get(0).getAsJsonObject().get("response").getAsString());
        Matcher matcher = mentionPattern.matcher(text);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            String username = matcher.group(1); // Extract the username
            String mention = "[@" + username + "]" + "(http://.)"; // Construct the mention format
            if(!Objects.equals(db.read("passwords", "password_" + username), "0")) matcher.appendReplacement(output, Matcher.quoteReplacement(mention));
        }
        matcher.appendTail(output);
        String txt = output.toString();
        String bk = "\\";
        for (String badWord : DiscordBot.badWords) {
            txt = txt.replaceAll("(?i)" + Pattern.quote(badWord), badWord.replaceAll(".", "#"));
        }
        return txt.replaceAll("\\\\n", "\n");
    }
}