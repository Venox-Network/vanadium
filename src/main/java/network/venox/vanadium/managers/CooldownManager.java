package network.venox.vanadium.managers;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@SuppressWarnings("unused")
public class CooldownManager {
    private final UUID player;
    private static final Map<UUID, Long> cooldown = new ConcurrentHashMap<>();

    public CooldownManager(Player player) {
        this.player = player.getUniqueId();
    }

    /**
     * Start {@code player}'s cooldown
     */
    public void start(Long time) {
        cooldown.put(player, System.currentTimeMillis() + time);
    }

    /**
     * Remove {@code player}'s cooldown
     */
    public void stop() {
        cooldown.remove(player);
    }

    /**
     * Resets {@code player}'s cooldown
     */
    public void reset(Long time) {
        stop();
        start(time);
    }

    /**
     * @return  {@code true} if on cooldown, {@code false} if not
     */
    public boolean onCooldown() {
        return timeLeft() > 0;
    }

    /**
     * @return  The amount of time left in the cooldown
     */
    public Long timeLeft() {
        return cooldown.get(player) - System.currentTimeMillis();
    }
}
