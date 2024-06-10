package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import spigey.bot.system.Command;
import spigey.bot.system.CommandInfo;
import spigey.bot.system.db;
import spigey.bot.system.sys;

import java.util.Objects;
import java.util.regex.Pattern;

@CommandInfo(
        slashCommand = "profile",
        usage = "[username]",
        description = "View someone's profile."
)
public class ProfileCommand implements Command {
    @Override
    public int slashCommand(SlashCommandInteractionEvent event) throws Exception {
        if(event.getGuild() == null){event.reply("Commands are disabled in DMs, tough luck mguy").queue(); return 0;}
        if(Objects.equals(db.read(event.getUser().getId(), "account"), "0")){event.reply("You need to be registered to use this command!").setEphemeral(true).queue(); return 0;}
        String user = db.read(event.getUser().getId(), "account");
        try{if(!Objects.equals(db.read(event.getOption("username").getAsString(), "password", "0"), "0")) user = event.getOption("username").getAsString();}catch(Exception e){/**/}
        final Button follow = Button.secondary("follow_" + user, "Follow").withEmoji(Emoji.fromUnicode("\ud83d\udc65"));
        String posts = event.getUser().getMutualGuilds().stream().anyMatch(guild -> guild.getIdLong() == 1246040271435730975L) ? db.read(user, "posts") : Pattern.compile("(\\[.*?])\\((.*?)\\)").matcher(db.read(user, "posts", "\nThis user has not posted yet.")).replaceAll("$1(https://discord.gg/VX2BJ7r9Xq)");
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Profile")
                .setDescription(String.format("**Name:** %s %s\n**Following:** %s\n**Followers:** %s\n**Status:** %s\n**Posts:**%s\n**Friends:** %s\n**Last Online:** <t:%s:R> (<t:%s>)", user, db.read(user, "verified", ""), sys.occur(db.read(user, "following"), ", "), sys.occur(db.read(user, "followers"), ", "), db.read(user, "status", "/"), posts, db.read(user, "friends", "No friends yet :("), db.read(user, "last_online"), db.read(user, "last_online"))).build();
        event.reply("").addEmbeds(embed).addActionRow(follow).queue();
        return 1;
    }
}