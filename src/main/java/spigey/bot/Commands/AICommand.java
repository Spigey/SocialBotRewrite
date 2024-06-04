package spigey.bot.Commands;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import spigey.bot.system.Command;
import spigey.bot.system.CommandInfo;
import spigey.bot.system.db;
import spigey.bot.system.util;

@CommandInfo(
        slashCommand = "ai",
        description = "Customize your personal AI.",
        usage = "<option> [personality]"
)
public class AICommand implements Command {
    @Override
    public int slashCommand(SlashCommandInteractionEvent event) throws Exception {
        if(event.getOption("option").getAsString().equals("customize")){
            if(event.getOption("personality") == null){event.reply("You have to select a personality with the `personality` option!").setEphemeral(true).queue(); return 0;}
            db.write(event.getUser().getId(), "ai_personality", event.getOption("personality").getAsString());
            event.reply("You have successfully updated the personality of your personal AI! You can talk to it by direct messaging me.").setEphemeral(true).queue();
            return 1;
        } else{
            db.remove(event.getUser().getId(), "ai_personality");
            event.reply("You have successfully reset your personal AI's personality! You can talk to it by direct messaging me.").setEphemeral(true).queue();
            return 1;
        }
    }
}