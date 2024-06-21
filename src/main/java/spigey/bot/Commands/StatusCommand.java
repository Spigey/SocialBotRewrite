package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.json.simple.parser.ParseException;
import oshi.hardware.NetworkIF;
import spigey.bot.system.*;
import oshi.SystemInfo;
import net.dv8tion.jda.api.entities.Guild;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicInteger;


@CommandInfo(
        slashCommand = "status",
        description = "Display the Bot's status."
)
public class StatusCommand implements Command {
    @Override
    public int slashCommand(SlashCommandInteractionEvent event) throws Exception {
        event.deferReply().queue();
        long gatewayPing = event.getJDA().getGatewayPing();
        long restPing = event.getJDA().getRestPing().complete();
        long memoryUsage = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
        SystemInfo si = new SystemInfo();
        double cpuLoad = si.getHardware().getProcessor().getSystemCpuLoadBetweenTicks(si.getHardware().getProcessor().getSystemCpuLoadTicks()) * 100;
        String osName = System.getProperty("os.name");
        String jdaVersion = JDAInfo.VERSION.replace(".20", "").replace("-alpha", "");
        String uptime = String.format(
                "%d h, %d min",
                ManagementFactory.getRuntimeMXBean().getUptime() / (1000 * 60 * 60),  // Hours
                (ManagementFactory.getRuntimeMXBean().getUptime() / (1000 * 60)) % 60      // Seconds
        );
        // ,  // Minutes
        //                (ManagementFactory.getRuntimeMXBean().getUptime() / 1000) % 60
        int threadCount = Thread.activeCount();
        int shardCount = event.getJDA().getShardInfo() != null ? event.getJDA().getShardInfo().getShardTotal() : 1;
        int currentShard = event.getJDA().getShardInfo() != null ? event.getJDA().getShardInfo().getShardId() : 0;
        int lines = sys.codeLines("src/main/java/spigey/bot/");
        int chars = sys.codeChars("src/main/java/spigey/bot");
        String commandsProcessed = db.read("properties", "cmds"); // wow, this is not AI generated
        NetworkIF[] networkIFs = si.getHardware().getNetworkIFs().toArray(new NetworkIF[0]);
        long bytesSent = networkIFs[0].getBytesSent();
        long bytesReceived = networkIFs[0].getBytesRecv();
        String osArch = System.getProperty("os.arch");
        String messagesProcessed = db.read("properties", "msgs");
        AtomicInteger status = new AtomicInteger();
        AtomicInteger userCount = new AtomicInteger();
        int totalUserCount = 0;
        if (event.getJDA().getShardManager() != null) { // Check if bot is sharded
            for (JDA shard : event.getJDA().getShardManager().getShards()) {
                totalUserCount += shard.getGuilds().stream()
                        .mapToInt(Guild::getMemberCount)
                        .sum();
            }
        } else { // Bot is not sharded
            totalUserCount = event.getJDA().getGuilds().stream()
                    .mapToInt(Guild::getMemberCount)
                    .sum();
        }
        int finalTotalUserCount = totalUserCount;
        String feeling;
        if (gatewayPing > 500 || restPing > 1000 || memoryUsage > 75 || cpuLoad > 7.5) {
            status.set(EmbedColor.RED);
            feeling = "HIGH RESOURCE USAGE";
        } else if (gatewayPing > 250 || restPing > 500 || memoryUsage > 50 || cpuLoad > 5) {
            status.set(EmbedColor.YELLOW);
            feeling = "MODERATE";
        } else {
            status.set(EmbedColor.GREEN);
            feeling = "OPERATIONAL";
        }
        int serverCount = event.getJDA().getGuilds().size();
        EmbedBuilder embed = null;
        try {
            embed = new EmbedBuilder()
                    .setTitle(event.getJDA().getSelfUser().getName() + " Status")
                    .addField(":computer: **Ram Usage**", String.format("```%s MB```\n\n:ping_pong: **Gateway Ping**\n```%s MS```\n\n:ping_pong: **Rest Ping**\n```%s MS```\n\n:rocket: **Server Count**\n```%s```\n\n:computer: **OS Arch**\n```%s```", memoryUsage, gatewayPing, restPing, serverCount, osArch), true)
                    .addField(":fire: **CPU Load**", String.format("```%.2f%%```\n\n:desktop: **Operating System**\n```%s```\n\n:robot: **JDA Version**\n```%s```\n\n:timer: **Uptime**\n```%s```\n\n:busts_in_silhouette: **Cached Users**\n```~%s```", cpuLoad, osName, jdaVersion, uptime, finalTotalUserCount), true)
                    .addField(":thread: **Thread Count**", String.format("```%s```\n\n:robot: **Shard Count**\n```%s```\n\n:robot: **Current Shard**\n```%s```\n\n:chart_with_upwards_trend: **Status**\n```%s```\n\n:cd: **Database Size**\n```%s Keys (%s KB)```", threadCount, shardCount, currentShard + 1, feeling, db.keySize(), "???"), true)
                    .addField(":page_facing_up: **Lines of Code**", String.format("```%s```", lines), true)
                    .addField(":globe_with_meridians: **Commands Processed**", String.format("```%s```", commandsProcessed), true)
                    .addField(":speech_balloon: **Messages Processed**", String.format("```%s```", messagesProcessed), true)
                    .addField(":incoming_envelope: **Bytes sent/received**", String.format("```%s/%s```", bytesSent, bytesReceived), true)
                    .addField(":page_facing_up: **Code Characters**", String.format("```%s```", chars), true)
                    .setColor(status.get());
        } catch (Exception L){
            sys.errInfo(L);
        }
        assert embed != null;
        event.getHook().sendMessage("").addEmbeds(embed.build()).queue();
        return 1;
    }
}