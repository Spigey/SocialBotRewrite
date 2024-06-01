package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import spigey.bot.system.Command;
import spigey.bot.system.CommandInfo;
import spigey.bot.system.EmbedColor;
import spigey.bot.system.db;

import static spigey.bot.DiscordBot.BotOwner;

@CommandInfo(
        buttonId = "logout"
)
public class LogOutHandlerCommand implements Command {
    @Override
    public void button(ButtonInteractionEvent event) throws Exception {
        if (!event.getMember().getId().equals(BotOwner)) return;
        String username = event.getMessage().getEmbeds().get(0).getDescription().split("`")[1];
        String user = event.getMessage().getContentRaw();
        db.remove(user, "account");
        event.reply("Successfully logged out " + username + ".").setEphemeral(true).queue();
        event.editButton(event.getButton().asDisabled()).queue();
        MessageEmbed punishLog = new EmbedBuilder()
                .setTitle(":door: Logged Out")
                .setDescription(String.format("%s (%s / %s) has been logged out by %s.",username, user, event.getJDA().retrieveUserById(user).complete().getAsTag(), event.getUser().getName()))
                .setColor(EmbedColor.DARK_GREY).build();
        event.getJDA().getGuildById("1219338270773874729").getTextChannelById("1246129804344823899").sendMessage("").addEmbeds(punishLog).queue();
    }
}