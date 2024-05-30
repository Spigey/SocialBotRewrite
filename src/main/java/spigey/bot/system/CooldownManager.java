package spigey.bot.system;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;

public class CooldownManager {
    private final Map<String, Long> cooldowns; // Map to store cooldown timestamps (user ID or guild ID to timestamp)
    private final long cooldownDurationMillis; // Cooldown duration in milliseconds

    public CooldownManager(long cooldownDurationMillis) {
        this.cooldowns = new HashMap<>();
        this.cooldownDurationMillis = cooldownDurationMillis;
    }

    public boolean isActive(User member) {
        String key = member.getId(); // Use member ID as the key in the cooldowns map
        return !(!cooldowns.containsKey(key) || System.currentTimeMillis() - cooldowns.get(key) >= cooldownDurationMillis);
    }

    public void update(User member) {
        String key = member.getId(); // Use member ID as the key in the cooldowns map
        cooldowns.put(key, System.currentTimeMillis());
    }

    public String parse(User member) {
        String key = member.getId(); // Use member ID as the key in the cooldowns map
        if (cooldowns.containsKey(key)) {
            long currentTime = System.currentTimeMillis();
            long lastExecutionTime = cooldowns.get(key);
            long remainingCooldownMillis = cooldownDurationMillis - (currentTime - lastExecutionTime);

            if (remainingCooldownMillis <= 0) {
                return "Cooldown expired";
            }

            long days = remainingCooldownMillis / (1000 * 60 * 60 * 24);
            long hours = (remainingCooldownMillis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
            long minutes = (remainingCooldownMillis % (1000 * 60 * 60)) / (1000 * 60);
            long seconds = (remainingCooldownMillis % (1000 * 60)) / 1000;

            StringBuilder parsedCooldown = new StringBuilder();
            if (days > 0) {
                parsedCooldown.append(days).append(" days, ");
            }
            if (hours > 0) {
                parsedCooldown.append(hours).append(" hours, ");
            }
            if (minutes > 0) {
                parsedCooldown.append(minutes).append(" minutes, ");
            }
            parsedCooldown.append(seconds).append(" seconds");

            return parsedCooldown.toString();
        } else {
            return "No cooldown";
        }
    }
}
