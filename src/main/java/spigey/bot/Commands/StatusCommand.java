package spigey.bot.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import spigey.bot.system.*;
import oshi.SystemInfo;
import net.dv8tion.jda.api.entities.Guild;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicInteger;


@CommandInfo(
        slashCommand = "status"
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
        String jdaVersion = JDAInfo.VERSION.replace(".20", "");
        String uptime = String.format(
                "%d h, %d min, %d sec",
                ManagementFactory.getRuntimeMXBean().getUptime() / (1000 * 60 * 60),  // Hours
                (ManagementFactory.getRuntimeMXBean().getUptime() / (1000 * 60)) % 60,  // Minutes
                (ManagementFactory.getRuntimeMXBean().getUptime() / 1000) % 60         // Seconds
        );
        int threadCount = Thread.activeCount();
        int shardCount = event.getJDA().getShardInfo() != null ? event.getJDA().getShardInfo().getShardTotal() : 1;
        int currentShard = event.getJDA().getShardInfo() != null ? event.getJDA().getShardInfo().getShardId() : 0;
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
        util.userExecF("*").thenAccept(users -> {
            for (User user : users) {
                userCount.getAndIncrement();
            }
        }).thenRun(() -> {
            String feeling;
            if (gatewayPing > 500 || restPing > 1000 || memoryUsage > 75) {
                status.set(EmbedColor.RED);
                feeling = "HIGH RESOURCE USAGE";
            } else if (gatewayPing > 250 || restPing > 500 || memoryUsage > 50) {
                status.set(EmbedColor.YELLOW);
                feeling = "MODERATE";
            } else {
                status.set(EmbedColor.GREEN);
                feeling = "OPERATIONAL";
            }
            int serverCount = event.getJDA().getGuilds().size();
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(event.getJDA().getSelfUser().getName() + " Status")
                    .addField(":computer: **Ram Usage**", String.format("```%s MB```\n\n:ping_pong: **Gateway Ping**\n```%s MS```\n\n:ping_pong: **Rest Ping**\n```%s MS```\n\n:rocket: **Server Count**\n```%s```\n\n:bust_in_silhouette: **Registered Users**\n```%s```", memoryUsage, gatewayPing, restPing, serverCount, userCount), true)
                    .addField(":fire: **CPU Load**", String.format("```%s%%```\n\n:desktop: **Operating System**\n```%s```\n\n:robot: **JDA Version**\n```%s```\n\n:timer: **Uptime**\n```%s```\n\n:busts_in_silhouette: **Cached Users**\n```~%s```", cpuLoad, osName, jdaVersion, uptime, finalTotalUserCount), true)
                    .addField(":thread: **Thread Count**", String.format("```%s```\n\n:robot: **Shard Count**\n```%s```\n\n:robot: **Current Shard**\n```%s```\n\n:chart_with_upwards_trend: **Status**\n```%s```\n\n:closed_book: **Database Size**\n```%s Keys```", threadCount, shardCount, currentShard, feeling, db.keySize()), true)
                    .setColor(status.get());
            event.getHook().sendMessage("").addEmbeds(embed.build()).queue();
        });
        return 1;
    }
}