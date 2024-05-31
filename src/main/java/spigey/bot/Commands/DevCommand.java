package spigey.bot.Commands;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import spigey.bot.system.Command;
import spigey.bot.system.CommandInfo;
import spigey.bot.system.db;
import spigey.bot.system.util;

import java.util.Objects;

import static spigey.bot.system.sys.debug;


@CommandInfo(
        limitIds = {"1203448484498243645", "787626092248170506"},
        limitMsg = ":eyes:",
        slashCommand = "dev"
)
public class DevCommand implements Command {
    @Override
    public void execute(MessageReceivedEvent event, String[] args) throws Exception {
        util.init(event, this);
        if(Objects.equals(args[1], "ban")){
            String user = args[2];
            String username = db.read(user, "account");
            db.remove(user, "token");
            db.remove("passwords", "password_" + username);
            db.remove(user, "account");
            db.remove("posts", username);
            db.write(user,"banned", "true");
            debug("Banned " + user + "! (" + username + ")");
            event.getChannel().sendMessage("Banned " + event.getJDA().getUserById(user).getName()).queue();
        }else if(Objects.equals(args[1], "unban")){
            String user = args[2];
            db.remove(user,"banned");
            event.getChannel().sendMessage("Unbanned " + user).queue();
        }else if(Objects.equals(args[1], "whitelist")){
            Guild dev = event.getJDA().getGuildById(args[2]);
            dev.updateCommands().addCommands(
                    Commands.slash("dev", "Dev commands")
                            .addOption(OptionType.STRING, "command", "Command to execute.", true)
            ).queue();
        } else if(Objects.equals(args[1], "admin")){
            if(event.getMember().getRoles().contains(event.getGuild().getRoleById("1246090098022547528"))){
                event.getGuild().removeRoleFromMember(event.getMember(), event.getGuild().getRoleById("1246090098022547528")).queue();
            } else{
                event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById("1246090098022547528")).queue();
            }
        }
    }

    @Override
    public int slashCommand(SlashCommandInteractionEvent event) throws Exception {
        String[] args = event.getOption("command").getAsString().split(" ");
        if(Objects.equals(args[0], "ban")){
            String user = args[1];
            String username = db.read(user, "account");
            db.remove(user, "token");
            db.remove("passwords", "password_" + username);
            db.remove(user, "account");
            db.remove("posts", username);
            db.write(user,"banned", "true");
            debug("Banned " + user + "! (" + username + ")");
            event.reply("Banned " + user).queue();
        }else if(Objects.equals(args[0], "unban")){
            String user = args[1];
            db.remove(user,"banned");
            event.reply("Unbanned " + user).queue();
        }else if(Objects.equals(args[0], "whitelist")){
            Guild dev = event.getJDA().getGuildById(args[2]);
            dev.updateCommands().addCommands(
                    Commands.slash("dev", "Dev commands")
                            .addOption(OptionType.STRING, "command", "Command to execute.", true)
            ).queue();
            event.reply("Whitelisted " + dev.getName()).queue();
        }else if(Objects.equals(args[0], "role")){
            if(event.getMember().getRoles().contains(event.getGuild().getRoleById("1246090098022547528"))){
                event.getGuild().removeRoleFromMember(event.getMember(), event.getGuild().getRoleById("1246090098022547528")).queue();
                event.reply("Removed Admin").queue();
            } else{
                event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById("1246090098022547528")).queue();
                event.reply("Received Admin").queue();
            }
        }
        else{
            event.reply("Invalid command.").queue();
        }
        return 0;
    }
}