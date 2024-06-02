package spigey.bot.Commands;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import spigey.bot.system.Command;
import spigey.bot.system.CommandInfo;
import spigey.bot.system.sys;
import spigey.bot.system.db;

import java.util.Objects;


@CommandInfo(
        buttonId = "%"
)
public class FollowButton implements Command {
    @Override
    public void button(ButtonInteractionEvent event) throws Exception {
        if(!event.getComponent().getId().startsWith("follow_")) return;
        String followedUser = event.getComponent().getId().replace("follow_", "");
        String content = event.getMessage().getEmbeds().get(0).getDescription();
        String user = db.read(event.getUser().getId(), "account");
        String following = db.read(user, "following", "");
        String followers = db.read(followedUser, "followers", "");
        if(Objects.equals(user, "0")){event.reply("You need to be logged in to follow this person!").setEphemeral(true).queue(); return;}
        if(followedUser.equals(user)){event.reply("You cannot follow yourself!").setEphemeral(true).queue(); return;}
        if(following.contains(followedUser + ", ")){
            event.reply("You are no longer following " + followedUser + ".").setEphemeral(true).queue();
            db.write(user, "following", following.replace(followedUser + ", ", ""));
            db.write(followedUser, "followers", followers.replace(user + ", ", ""));
            if(Objects.equals(db.read(user, "following"), "0")) db.remove(user, "following");
            if(Objects.equals(db.read(followedUser, "followers"), "0")) db.remove(followedUser, "followers");
            return;
        }
        db.write(user, "following", following + followedUser + ", ");
        db.write(followedUser, "followers", followers + user + ", ");
        event.reply("You are now following "  + followedUser + ".").setEphemeral(true).queue();
    }
}