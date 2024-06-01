package spigey.bot.Commands;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import spigey.bot.system.Command;
import spigey.bot.system.CommandInfo;
import spigey.bot.system.db;

import static spigey.bot.system.util.msg;

@CommandInfo(
        slashCommand = "setchannel",
        description = "Select a channel to send posts to.",
        usage = "<channel>"
)
public class SetChannelCommand implements Command {
    @Override
    public int slashCommand(SlashCommandInteractionEvent event) throws Exception {
        if(event.getGuild() == null){event.reply("Commands are disabled in DMs, tough luck mguy").queue(); return 0;}
        if(!event.getMember().hasPermission(Permission.MANAGE_CHANNEL)){event.reply(":x: You require the `MANAGE_CHANNEL` permission to be able to use this command!").setEphemeral(true).queue(); return 0;}
        if(event.getOption("channel").getAsChannel().getType() != ChannelType.TEXT){event.reply(":x: You have to specify a valid text channel!").setEphemeral(true).queue(); return 0;}
        db.write("channels", event.getGuild().getId(), event.getOption("channel").getAsString());
        event.reply("The post channel for this server has been updated to " + event.getOption("channel").getAsChannel().getAsMention() + "!").queue();
        return 1;
    }
}