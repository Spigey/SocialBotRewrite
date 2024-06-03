package spigey.bot.Commands.devonly.button;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import spigey.bot.system.*;

import java.util.Collections;
import java.util.List;

import static spigey.bot.DiscordBot.BotOwner;

@CommandInfo(
        buttonId = "ban"
)
public class BanHandlerCommand implements Command {
    @Override
    public void button(ButtonInteractionEvent event) throws Exception {
        if (!event.getMember().getId().equals(BotOwner)) return;
        String username = event.getMessage().getEmbeds().get(0).getDescription().split("`")[1];
        String user = event.getMessage().getContentRaw();
        if(!event.getChannel().getId().equals("1246117906547347578")){
            db.remove(username);
        }
        util.userExec(username, User -> {
            db.remove(user, "token");
            db.remove(user, "account");
        });
        db.write(user, "banned", "true");
        List<ActionRow> emptyActionRows = Collections.emptyList();
        event.reply("Successfully banished " + username + ".").setEphemeral(true).queue();
        event.editComponents(emptyActionRows).queue();
        MessageEmbed punishLog = new EmbedBuilder()
                .setTitle(":zap: Banished!")
                .setDescription(String.format("%s (%s / %s) has been **Banished** by %s!", username, user, event.getJDA().retrieveUserById(user).complete().getAsTag(), event.getUser().getName()))
                .setColor(EmbedColor.GOLD).build();
        event.getJDA().getGuildById("1219338270773874729").getTextChannelById("1246129804344823899").sendMessage("").addEmbeds(punishLog).queue();
    }
}