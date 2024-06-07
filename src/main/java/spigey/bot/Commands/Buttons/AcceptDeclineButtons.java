package spigey.bot.Commands.Buttons;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import spigey.bot.system.*;

import java.util.Collections;
import java.util.Objects;

@CommandInfo(
        buttonId = "%"
)
public class AcceptDeclineButtons implements Command {
    @Override
    public void button(ButtonInteractionEvent event) throws Exception {
        if(!event.getButton().getId().startsWith("accept") && !event.getButton().getId().startsWith("decline")) return;
        String username = event.getButton().getId().split("Ä")[1];
        String self = event.getButton().getId().split("Ä")[2];
        String user = db.read(event.getUser().getId(), "account");
        if(Objects.equals(user, "0")){event.reply("You are not logged in!").setEphemeral(true).queue(); return;}
        if(!Objects.equals(self, user)){event.reply("You do not have a pending friend request from this user.").setEphemeral(true).queue();}
        if(event.getButton().getId().startsWith("accept")){
            db.write(self, "friends", String.format("%s, %s", db.read(self, "friends", ""), username));
            db.write(username, "friends", String.format("%s, %s", db.read(username, "friends", ""), self));
            db.write(username, "notifications", "accept-" + self + "," + db.read(username, "notifications"));
            event.reply("You have accepted the friend request from " + username + "!").setEphemeral(true).queue();
            event.getMessage().editMessageComponents(Collections.emptyList()).queue();
        }else {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Friend request declined")
                    .setDescription(self + " has declined your friend request. :(")
                    .setColor(EmbedColor.RED);
            util.notif(username, embed.build());
            event.reply("You have declined the friend request from " + username + ".").setEphemeral(true).queue();
            event.getMessage().editMessageComponents(Collections.emptyList()).queue();
        }
    }
}