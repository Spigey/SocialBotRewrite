package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import spigey.bot.system.*;

import java.io.File;
import java.util.Objects;


@CommandInfo(
        slashCommand = "help",
        description = "Explore a list of available commands and their usage."
)
public class HelpCommand implements Command {
    @Override
    public int slashCommand(SlashCommandInteractionEvent event) throws Exception {
        File[] files = new File(getClass().getClassLoader().getResource("spigey/bot/Commands").getFile()).listFiles((dir, name) -> name.endsWith(".class"));
        StringBuilder desc = new StringBuilder();
        for (File file : files) {
            String className = file.getName().replace(".class", "");
            Class<?> clazz = Class.forName("spigey.bot.Commands." + className);
            CommandInfo commandInfo = clazz.getAnnotation(CommandInfo.class);
            if(commandInfo == null) continue;
            if (!Objects.equals(commandInfo.slashCommand(), "") && !Objects.equals(commandInfo.slashCommand(), "dev")) {
                desc.append(String.format("**`/%s %s`**:\n" + EmojiDB.NoPickaxe + "\u255a %s\n", commandInfo.slashCommand(), commandInfo.usage() , commandInfo.description()).replaceAll(" USAGE",""));
            }
        }
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Help Menu")
                .setDescription("**[] - Optional, <> - Required**\n" + desc)
                .setColor(EmbedColor.BLUE)
                .setThumbnail("https://cdn.discordapp.com/avatars/1245818951519305750/88ef8e77f4a090b0dabdb3a80bb1e831.webp?size=128");
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        return 1;
    }
}