package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import spigey.bot.system.Command;
import spigey.bot.system.CommandInfo;
import spigey.bot.system.EmbedColor;
import spigey.bot.system.db;

@CommandInfo(
        slashCommand = "logout",
        description = "Log out of the bot to log into another account."
)
public class LogoutCommand implements Command {
    @Override
    public int slashCommand(SlashCommandInteractionEvent event) throws Exception {
        if(db.read(event.getUser().getId(), "account").equals("0")){event.reply(":x: You are not logged in!").setEphemeral(true).queue(); return 0;}
        db.remove(event.getUser().getId(), "account");
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(":white_check_mark: Logged out")
                .setDescription("You have been successfully logged out!")
                .setColor(EmbedColor.GREEN);
        event.reply("").addEmbeds(embed.build()).setEphemeral(true).queue();
        return 1;
    }
}