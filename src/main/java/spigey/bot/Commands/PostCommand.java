package spigey.bot.Commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
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
import java.util.stream.Collectors;

import static spigey.bot.DiscordBot.badWords;
import static spigey.bot.DiscordBot.jda;
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
        if(Objects.equals(db.read(db.read(event.getUser().getId(), "account"), "verified"), "0")) if(event.getOption("attachment") != null && !event.getOption("attachment").getAsAttachment().getContentType().contains("image/")){sys.warn(event.getUser().getId() + " attempted to upload " + event.getOption("attachment").getAsAttachment().getContentType()); event.reply("You are not allowed to post this file type!").setEphemeral(true).queue(); return 0;}
        JSONArray channelsArray = db.getArray("channels");
        String username = db.read(event.getUser().getId(), "account");
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(String.format("New post by @%s %s", username, db.read(username, "verified", "")))
                .setDescription(txt(event.getOption("content").getAsString()))
                .setColor(EmbedColor.BLURPLE)
                .setTimestamp(Instant.now());
        if(event.getOption("attachment") != null) embed.setImage(event.getOption("attachment").getAsAttachment().getProxyUrl());
        // Buttons
        final Button follow = Button.secondary("follow_" + username, "Follow").withEmoji(Emoji.fromUnicode("\ud83d\udc65"));
        final Button report = Button.danger("report_" + username, "Report").withEmoji(Emoji.fromUnicode("\ud83d\uded1"));

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
                            finalPost.sendMessage("").addEmbeds(embed.build()).addFiles(FileUpload.fromData(attachment)).addActionRow(follow, report).queue();}
                        else{
                            try {
                                EmbedBuilder img = new EmbedBuilder()
                                        .setTitle(String.format("New post by @%s %s", username, db.read(username, "verified", "")))
                                        .setDescription(txt(event.getOption("content").getAsString()))
                                        .setColor(EmbedColor.BLURPLE)
                                        .setImage(event.getOption("attachment").getAsAttachment().getProxyUrl())
                                        .setTimestamp(Instant.now());
                                finalPost.sendMessage("").addEmbeds(img.build()).addActionRow(follow, report).queue();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                        file.delete();
                    });
                    continue;
                }
                try{post.sendMessage("").addEmbeds(embed.build()).addActionRow(follow, report).queue();} catch(InsufficientPermissionException e){sys.warn("Failed to post to guild " + event.getJDA().getGuildById(post.getGuild().getId()).getName() + ": Insufficient Permissions!");}
            }
        }
        Pattern mentionPattern = Pattern.compile("@(\\w[\\w._]*)");
        Matcher matcher = mentionPattern.matcher(event.getOption("content").getAsString());
        Set<String> mentionedUsernames = new HashSet<>();
        while (matcher.find()) mentionedUsernames.add(matcher.group(1));
        for (String mentionedUsername : mentionedUsernames) {
            /* EmbedBuilder pingEmbed = new EmbedBuilder()
                    .setTitle("New Mention!")
                    .setDescription("**You were mentioned in a post by " + username + ":**\n" + txt(event.getOption("content").getAsString()))
                    .setColor(EmbedColor.GOLD);
            util.notif(mentionedUsername, pingEmbed.build()); */
            db.write(mentionedUsername, "notifications", "mention-" + username + "," + db.read(username, "notifications"));
        }
        EmbedBuilder followEmbed = new EmbedBuilder()
                .setTitle(String.format("@%s has posted!", username))
                .setDescription(txt(event.getOption("content").getAsString()))
                .setColor(EmbedColor.BLURPLE)
                .setTimestamp(Instant.now());
        String[] users = db.read(username, "followers", "").split(", ");
        String userString = db.read(username, "followers", "");
        for(String user : users){
            if(!util.notif(user, followEmbed.build())){
                userString = userString.replace(user + ", ", ", ");
            }
        }
        db.write(username, "followers", userString);

        AtomicReference<String> url = new AtomicReference<>("");
        event.getJDA().getGuildById("1246040271435730975").getTextChannelById("1246040631801810986").sendMessage("").addEmbeds(embed.build()).addActionRow(follow, report).queue(message -> {
            url.set(message.getJumpUrl());
            try {
                StringBuffer sb = new StringBuffer();
                Matcher dkjdfh = Pattern.compile("<@!?(\\d+)>").matcher(event.getOption("content").getAsString());
                while(dkjdfh.find()){
                    try{dkjdfh.appendReplacement(sb, "@" + jda.retrieveUserById(dkjdfh.group(1)).complete().getName());}
                    catch(ErrorResponseException L){dkjdfh.appendReplacement(sb, "@unkno...");}
                }
                dkjdfh.appendTail(sb);
                db.write(username, "posts", sys.trimMarkdown("\n[" + sys.trim(sb.toString(),10) +"](" + url.get() + ")" + db.read(username, "posts", ""), 10));
            } catch (Exception e) {/**/}
        });
        if(!Objects.equals(db.read(username, "verified"), "0")) return 0;
        return 1;
    }


    private static String txt(String text) throws Exception {
        StringBuffer sb = new StringBuffer();
        Matcher dkjdfh = Pattern.compile("<@!?(\\d+)>").matcher(text);
        while(dkjdfh.find()){
            try{dkjdfh.appendReplacement(sb, "@" + jda.retrieveUserById(dkjdfh.group(1)).complete().getName());}
            catch(ErrorResponseException L){dkjdfh.appendReplacement(sb, "@unknown-user");}
        }
        dkjdfh.appendTail(sb);
        Pattern mentionPattern = Pattern.compile("@(\\w[\\w._]*)");
        // Matcher matcher = mentionPattern.matcher(new Gson().fromJson(sys.sendApiRequest("https://api.kastg.xyz/api/ai/chatgptV4?prompt=" + URLEncoder.encode("You're a Moderation AI. Your task is to keep posts PG13 by censoring bad words by replacing every single letter with `\\*`. Make sure to always double check that you have censored actual BAD words, and no good words. For example: `you are a retard` -> `you are a \\*\\*\\*\\*\\*\\*`, Do not censor \"fuck\" and \"shit\". If the prompt does not contain any bad words, just return the prompt again. You can ONLY reply in the censored prompt, the users prompt is: " + text + "`", StandardCharsets.UTF_8), "GET", null, null), JsonObject.class).getAsJsonArray("result").get(0).getAsJsonObject().get("response").getAsString());
        Matcher matcher = mentionPattern.matcher(sb.toString());
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            String username = matcher.group(1); // Extract the username
            String mention = "[@" + username + "]" + "(http://.)"; // Construct the mention format
            if(!Objects.equals(db.read(username, "password"), "0")) matcher.appendReplacement(output, Matcher.quoteReplacement(mention));
        }
        matcher.appendTail(output);
        String txt = output.toString();
        String bk = "\\";

        txt = Pattern.compile("discord(?:.gg|.com/invite)/\\S+\\)?").matcher(txt).replaceAll("discord.gg/VX2BJ7r9Xq");

        /* for (String badWord : DiscordBot.badWords) {
            txt = txt.replaceAll("(?i)" + Pattern.quote(badWord), badWord.replaceAll(".", "#"));
        } */

        txt = Arrays.stream(txt.split("\\s+"))
                .map(word -> badWords.stream().anyMatch(bad -> word.toLowerCase().contains(bad))
                        ? word.replaceAll(".", "\\\\*")
                        : word)
                .collect(Collectors.joining(" "));

        return txt.replaceAll("\\\\n", "\n");
    }
}