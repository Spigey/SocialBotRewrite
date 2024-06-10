package spigey.bot.Commands.devonly.button;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import spigey.bot.system.*;

import static spigey.bot.DiscordBot.BotOwner;
import static spigey.bot.DiscordBot.jda;

@CommandInfo(
        buttonId = "snipe"
)
public class AccountHandlerCommand implements Command {
    @Override
    public void button(ButtonInteractionEvent event) throws Exception {
        if (!event.getMember().getId().equals(BotOwner)) return;
        String username = event.getMessage().getEmbeds().get(0).getDescription().split("`")[1];
        String user = event.getMessage().getContentRaw();
        sys.debug(util.userExec(username, UserN -> {
            db.remove(UserN.getId(), "account");
            sys.debug("Removed key account for user " + jda.retrieveUserById(UserN.getId()).complete().getName());
        }));
        sys.debug("asdasdsad");
        db.remove(username);
        event.reply(username + " has been sniped successfully.").setEphemeral(true).queue();
        event.editButton(event.getButton().asDisabled()).queue();
        MessageEmbed punishLog = new EmbedBuilder()
                .setTitle(":gun: Sniped!")
                .setDescription(String.format("%s (%s / %s) has been sniped by %s!",username, user, event.getJDA().retrieveUserById(user).complete().getAsTag(), event.getUser().getName()))
                .setColor(EmbedColor.RED).build();
        event.getJDA().getGuildById("1219338270773874729").getTextChannelById("1246129804344823899").sendMessage("").addEmbeds(punishLog).queue();
    }
}