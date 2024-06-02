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
        MessageEmbed embed = new EmbedBuilder()
                .setTitle(String.format("%s's Profile", user))
                .setDescription(String.format("**Following:** %s\n**Followers:** %s\n**Posts:**%s", sys.occur(db.read(user, "following"), ", "), sys.occur(db.read(user, "followers"), ", "), db.read(user, "posts", "\nThis user has not posted yet."))).build();
        event.reply("").addEmbeds(embed).addActionRow(follow).queue();
        return 1;
    }
}