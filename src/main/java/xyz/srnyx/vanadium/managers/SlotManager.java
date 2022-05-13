package xyz.srnyx.vanadium.managers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;

import xyz.srnyx.vanadium.Main;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class SlotManager {
    private String type;
    private Player player;
    private OfflinePlayer op;
    private static final Map<UUID, Long> locksCooldown = new HashMap<>();
    private static final Map<UUID, Long> trustsCooldown = new HashMap<>();

    /**
     * Constructor for SlotManager
     *
     * @param   type    The type of slot ({@code locks} or {@code trusts})
     * @param   player  The player to manage
     */
    public SlotManager(String type, Object player) {
        if (type != null) this.type = type;
        if (player instanceof Player player2) this.player = player2;
        if (player instanceof OfflinePlayer offlinePlayer) this.op = offlinePlayer;
    }

    /**
     * Constructor for SlotManager
     *
     * @param   type    The type of slot ({@code locks} or {@code trusts})
     */
    public SlotManager(String type) {
        if (type != null) this.type = type;
    }

    /**
     * Start {@code player}'s cooldown
     */
    public void start() {
        final long value = System.currentTimeMillis() + (TimeUnit.MINUTES.toMillis(Main.config.getInt("slot-cooldowns." + type)));
        if (Objects.equals(type, "locks")) locksCooldown.put(player.getUniqueId(), value);
        if (Objects.equals(type, "trusts")) trustsCooldown.put(player.getUniqueId(), value);
    }

    /**
     * Remove {@code player}'s cooldown
     */
    public void stop() {
        if (Objects.equals(type, "locks")) locksCooldown.remove(player.getUniqueId());
        if (Objects.equals(type, "trusts")) trustsCooldown.remove(player.getUniqueId());
    }

    /**
     * @return  The amount of time left in the cooldown
     */
    public Long timeLeft() {
        if (new SlotManager(type, player).contains()) {
            if (Objects.equals(type, "locks")) return locksCooldown.get(player.getUniqueId()) - System.currentTimeMillis();
            if (Objects.equals(type, "trusts")) return trustsCooldown.get(player.getUniqueId()) - System.currentTimeMillis();
        }
        return null;
    }

    /**
     * @return  True if cooldown map contains player, false if not
     */
    public boolean contains() {
        if (Objects.equals(type, "locks")) return locksCooldown.containsKey(player.getUniqueId());
        if (Objects.equals(type, "trusts")) return trustsCooldown.containsKey(player.getUniqueId());
        return false;
    }

    /**
     * Add/remove (a) slot(s) to the player
     */
    private void addSlot() {
        stop();
        start();

        int count = getMultiplier();
        final int[] data = DataManager.slots.get(player.getUniqueId());
        if (Objects.equals(type, "locks")) {
            count = (data != null ? data[0] : 0) + getMultiplier();
            DataManager.slots.put(player.getUniqueId(), new int[]{count, new SlotManager("trusts", player).getCount()});
        }
        if (Objects.equals(type, "trusts")) {
            count = (data != null ? data[1] : 0) + getMultiplier();
            DataManager.slots.put(player.getUniqueId(), new int[]{new SlotManager("locks", player).getCount(), count});
        }

        new MessageManager("slots.add")
                .replace("%count%", String.valueOf(getMultiplier()).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                .replace("%type%", type.substring(0, type.length() - 1))
                .replace("%slot%", getMultiplier() == 1 ? "slot" : "slots")
                .replace("%total%", String.valueOf(count).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                .replace("%next%", String.valueOf(Main.config.getInt("slot-cooldowns." + type)))
                .replace("%minute%", Main.config.getInt("slot-cooldowns." + type) == 1 ? "minute" : "minutes")
                .send(player);
    }

    /**
     * Adds/removes slots when the cooldown reaches 0
     */
    public void check() {
        new BukkitRunnable() {
            public void run() {
                for (final Player online : Bukkit.getOnlinePlayers()) {
                    SlotManager slot = new SlotManager(type, online);
                    if (slot.contains() && (slot.timeLeft() == null || slot.timeLeft() <= 0)) {
                        if (PlayerManager.isAFK(online)) {
                            slot.stop();
                            slot.start();
                        } else slot.addSlot();
                    }
                }
            }
        }.runTaskTimer(Main.plugin, 0, 1);
    }

    /**
     * @return  The {@code player}'s slot multiplier <i>({@code vanadium.multiplier.X})</i>
     */
    private int getMultiplier() {
        for (final PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String perm = pai.getPermission();
            if (perm.startsWith("vanadium.multiplier.")) {
                try {
                    return Integer.parseInt(perm.substring(perm.lastIndexOf(".") + 1));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return 1;
                }
            }
        }
        return 1;
    }

    /**
     * @return  The number of {@code type} slots the player has
     */
    public int getCount() {
        int[] slots = DataManager.slots.get(op.getUniqueId());
        int id = Objects.equals(type, "trusts") ? 1 : 0;
        return slots != null ? slots[id] : 0;
    }
}
