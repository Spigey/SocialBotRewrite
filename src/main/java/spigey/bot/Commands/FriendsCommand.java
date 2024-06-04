package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import spigey.bot.system.*;

import java.util.Objects;


@CommandInfo(
        slashCommand = "friends"
)
public class FriendsCommand implements Command {
    @Override
    public int slashCommand(SlashCommandInteractionEvent event) throws Exception {
        if (Objects.equals(db.read(event.getUser().getId(), "account"), "0")) {
            event.reply("You have to be logged in to use this command!").setEphemeral(true).queue();
            return 0;
        }
        if (Objects.equals(event.getSubcommandName(), "add")) {
            String username = db.read(event.getUser().getId(), "account");
            String adding = event.getOption("username").getAsString();
            String ownFriends = db.read(username, "friends", "");
            String addingFriends = db.read(adding, "friends", "");
            if (ownFriends.contains(adding + ", ") || addingFriends.contains(username + ", ")) {
                event.reply("You are already friends with this user. Believe this is a bug?").setEphemeral(true).queue();
                return 0;
            }
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("New Friend Request")
                    .setDescription("You have received a new friend request by **@" + username + "**!")
                    .setColor(EmbedColor.GREEN);
            final Button accept = Button.success(String.format("acceptÄ%sÄ%s", username, adding), "Accept");
            final Button decline = Button.danger(String.format("declineÄ%sÄ%s", username, adding), "Decline");
            event.reply("You have successfully sent a friend request to " + adding + "!").setEphemeral(true).queue();
            util.notif(adding, embed.build(), accept, decline);
        } else if (Objects.equals(event.getSubcommandName(), "remove")) {
            String username = db.read(event.getUser().getId(), "account");
            String removing = event.getOption("username").getAsString();
            String ownFriends = db.read(username, "friends");
            String removingFriends = db.read(removing, "friends");
            if (!(ownFriends.contains(removing + ", ") || removingFriends.contains(username + ", "))) {
                event.reply("You aren't friends with this user. Believe this is a bug?").setEphemeral(true).queue();
                return 0;
            }
            db.write(username, "friends", ownFriends.replaceAll(removing + ", ", ""));
            db.write(removing, "friends", removingFriends.replaceAll(username + ", ", ""));
            if (Objects.equals(db.read(removing, "friends"), "")) db.remove(removing, "friends");
            if (Objects.equals(db.read(username, "friends"), "")) db.remove(username, "friends");
            event.reply("You are now no longer friends with " + removing + ".").setEphemeral(true).queue();
        } else if (Objects.equals(event.getSubcommandName(), "list")) {
            String username = db.read(event.getUser().getId(), "account");
            String friends = sys.replaceXth(db.read(username, "friends"), ", ", "\n", 3);
            if (Objects.equals(friends, "0")) {
                event.reply("You currently have no friends on " + event.getJDA().getSelfUser().getName() + ".").setEphemeral(true).queue();
                return 0;
            }
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Friends list")
                    .setDescription("**Your current friends on " + event.getJDA().getSelfUser().getName() + " are:**\n" + friends)
                    .setColor(EmbedColor.BLURPLE);
            event.reply("").setEmbeds(embed.build()).queue();
        }

        return 1;
    }
}