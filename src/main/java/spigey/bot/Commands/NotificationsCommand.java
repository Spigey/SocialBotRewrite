package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import spigey.bot.system.*;

import java.util.Objects;


@CommandInfo(
        slashCommand = "notifications",
        description = "View your notifications."
)
public class NotificationsCommand implements Command {
    @Override
    public int slashCommand(SlashCommandInteractionEvent event) throws Exception {
        if(event.getGuild() == null){event.reply("Commands are disabled in DMs, tough luck mguy").queue(); return 0;}
        if(Objects.equals(db.read(event.getUser().getId(), "account"), "0")){event.reply("You need to be registered to use this command!").setEphemeral(true).queue(); return 0;}
        if(db.read(db.read(event.getUser().getId(), "account"), "notifications").equals("0")){event.reply("You currently do not have any new notifications.").setEphemeral(true).queue(); return 0;}
        String[] notifs = db.read(db.read(event.getUser().getId(), "account"), "notifications").replaceAll(",0", "").split(",");
        StringBuilder sb = new StringBuilder();
        String username = db.read(event.getUser().getId(), "account");
        for(String notif : notifs){
            if(notif.split("-").length == 2){
                switch (notif.split("-")[0]) {
                    case "follow" ->
                            sb.append("\n" + EmojiDB.follower + " **@").append(notif.split("-")[1]).append("** is now following you!");
                    case "mention" ->
                            sb.append("\n" + EmojiDB.mention + " **@").append(notif.split("-")[1]).append("** has mentioned you in one of their posts.");
                    case "accept" ->
                            sb.append("\n" + EmojiDB.add + " **@").append(notif.split("-")[1]).append("** has accepted your friend request!");
                    case "post" ->
                            sb.append("\n:speech_balloon: **@").append(notif.split("-")[1]).append("** has posted!");
                    default ->
                            sb.append("\n").append(notif);
                }
            } else{
                if(notif.equals("verified")){
                    if(!db.read(username, "verified").equals("0")) sb.append("\n").append(db.read(username, "verified")).append(" You are now verified!");
                } else if (notif.equals("post-removed")) {
                    sb.append("\n:octagonal_sign: One of your posts has been removed.");
                } else {
                    sb.append("\n").append(notif);
                }
            }
        }
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(":bell: Notifications (" + sys.occur(sb.toString(), "\n") + ")")
                .setDescription(sb.substring(1));
        event.reply("").addEmbeds(embed.build()).addActionRow(Button.secondary("read", "Mark As Read")).setEphemeral(true).queue();
        return 1;
    }
}