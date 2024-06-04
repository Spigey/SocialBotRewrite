package spigey.bot.system;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.OutputStream;
import java.io.PrintStream;

public class DiscordPrintStream extends PrintStream {
    private final JDA jda;
    private final TextChannel channel;

    public DiscordPrintStream(OutputStream out, JDA jda, TextChannel channel) {
        super(out);
        this.jda = jda;
        this.channel = channel;
    }

    @Override
    public void println(String x) {
        super.println(x);
        try {
            channel.sendMessage("```" + x + "```").queue();
        }catch(Exception L){/**/}
    }
}