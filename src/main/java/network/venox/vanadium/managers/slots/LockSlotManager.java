package network.venox.vanadium.managers.slots;

import network.venox.vanadium.Main;
import network.venox.vanadium.managers.DataManager;
import network.venox.vanadium.managers.MessageManager;
import network.venox.vanadium.managers.PlayerManager;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class LockSlotManager {
    private Player player;
    private UUID uuid;
    private static final Map<UUID, Long> cooldown = new ConcurrentHashMap<>();

    /**
     * Constructor for LockSlotManager
     *
     * @param   player  The player to manage
     */
    public LockSlotManager(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
    }

    /**
     * Constructor for LockSlotManager
     */
    public LockSlotManager() {}

    /**
     * Start {@code player}'s cooldown
     */
    public void start() {
        cooldown.put(uuid, System.currentTimeMillis() + (TimeUnit.MINUTES.toMillis(Main.config.getInt("slot-cooldowns.locks"))));
    }

    /**
     * Remove {@code player}'s cooldown
     */
    public void stop() {
        cooldown.remove(uuid);
    }

    /**
     * @return  The amount of time left in the cooldown
     */
    public Long timeLeft() {
        return contains() ? cooldown.get(uuid) - System.currentTimeMillis() : null;
    }

    /**
     * @return  True if cooldown map contains player, false if not
     */
    private boolean contains() {
        return cooldown.containsKey(uuid);
    }

    /**
     * Add/remove (a) lock slot(s) to the player
     */
    private void addSlot() {
        final int multiplier = new SlotManager().getMultiplier(player);
        final int[] data = DataManager.slots.get(uuid);
        final int count = (data != null ? data[0] : 0) + multiplier;

        stop();
        start();
        DataManager.slots.put(uuid, new int[]{count, new TrustSlotManager().getCount(uuid)});

        new MessageManager("slots.add")
                .replace("%count%", String.valueOf(multiplier).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                .replace("%type%", "lock")
                .replace("%slot%", multiplier == 1 ? "slot" : "slots")
                .replace("%total%", String.valueOf(count).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                .replace("%next%", String.valueOf(Main.config.getInt("slot-cooldowns.locks")))
                .replace("%minute%", Main.config.getInt("slot-cooldowns.locks") == 1 ? "minute" : "minutes")
                .send(player);
    }

    /**
     * Adds/removes slots when the cooldown reaches 0
     */
    public void check() {
        if (PlayerManager.isAFK(player)) {
            stop();
            start();
            return;
        }

        if (contains() && (timeLeft() == null || timeLeft() <= 0)) addSlot();
    }

    /**
     * @return  The number of lock slots the player has
     */
    public int getCount(UUID id) {
        final int[] slots = DataManager.slots.get(id);
        return slots != null ? slots[0] : 0;
    }
}
