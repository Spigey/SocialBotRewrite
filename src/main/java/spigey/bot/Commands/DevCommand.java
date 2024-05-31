package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import spigey.bot.system.*;

import java.util.Objects;

import static spigey.bot.system.sys.debug;


@CommandInfo(
        limitIds = {"1203448484498243645", "787626092248170506", "941366409399787580"},
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
        } else if(Objects.equals(args[0], "manage")){
            String user = args[1].replaceAll("--self", event.getUser().getId());
            // if(Objects.equals(db.read("passwords", "password_" + db.read(args[1], "account")), "0")){event.reply("User not found.").setEphemeral(true).queue(); return 0;}
            String username = db.read(user, "account", "???");
            String password = db.read("passwords", "password_" + username, "???");
            String decryptedPassword = sys.decrypt(db.read("passwords", "password_" + username, "???"),env.ENCRYPTION_KEY);
            String User = "???";
            try{
                User = String.format("%s (%s)", event.getJDA().getUserById(user).getAsTag(), user);
            }catch(Exception L){/**/}
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Account management Panel")
                    .setDescription(String.format("**Username**: `%s`\n**User**: `%s`\n**Password**: `%s`\n**Decrypted Password**: ||`%s`||\n**Password Strength**: `%s%%`\n**Token length**: `%s`", username, User, sys.passToStr(password, "*"), decryptedPassword, sys.passStrength(decryptedPassword),sys.decrypt(db.read(user, "token", ""), env.ENCRYPTION_KEY).length()))
                    .setColor(EmbedColor.RED);
            if(args.length > 2 && Objects.equals(args[2], "--ephemeral")){
                event.reply(user).addEmbeds(embed.build()).addActionRow(
                        Button.danger("snipe", "Snipe"),
                        Button.danger("ban", "Ban"),
                        Button.success("verify", "Verify"),
                        Button.secondary("logout", "Log Out")
                ).setEphemeral(true).queue();
            } else{
                event.reply(user).addEmbeds(embed.build()).addActionRow(
                        Button.danger("snipe", "Snipe"),
                        Button.danger("ban", "Ban"),
                        Button.success("verify", "Verify"),
                        Button.secondary("logout", "Log Out")
                ).queue();
            }
        } else if (Objects.equals(args[0], "purge")) {
            event.getChannel().getHistory().retrievePast(Integer.parseInt(args[1])).queue(messages -> {
                event.getChannel().purgeMessages(messages);
            });
            event.reply("Purging messages").setEphemeral(true).queue();
        } else if(Objects.equals(args[0], "error")){
            throw new Exception(args[1]);
        }
        else{
            event.reply("Invalid command.").queue();
        }
        return 0;
    }
}