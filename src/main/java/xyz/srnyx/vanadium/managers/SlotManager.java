package xyz.srnyx.vanadium.managers;

import com.earth2me.essentials.Essentials;

import org.bukkit.Bukkit;
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
    private Player player;
    private final String type;

    private static final Map<UUID, Long> locksCooldown = new HashMap<>();
    private static final Map<UUID, Long> trustsCooldown = new HashMap<>();

    public SlotManager(String type, Player player) {
        this.type = type;
        this.player = player;
    }

    public SlotManager(String type) {
        this.type = type;
    }

    /**
     * Start {@code player}'s cooldown
     */
    public void start() {
        long value = System.currentTimeMillis() + (TimeUnit.MINUTES.toMillis(Main.config.getInt("slot-cooldowns." + type)));
        if (Objects.equals(type, "locks")) locksCooldown.put(player.getUniqueId(), value);
        if (Objects.equals(type, "trusts")) trustsCooldown.put(player.getUniqueId(), value);
    }

    /**
     * Remove {@code player}'s cooldown
     */
    public void stop() {
        UUID uuid = player.getUniqueId();
        if (Objects.equals(type, "locks")) locksCooldown.remove(uuid);
        if (Objects.equals(type, "trusts")) trustsCooldown.remove(uuid);
    }

    /**
     * @return  The amount of time left in the cooldown
     */
    public Long timeLeft() {
        if (Objects.equals(type, "locks")) return locksCooldown.get(player.getUniqueId()) - System.currentTimeMillis();
        if (Objects.equals(type, "trusts")) return trustsCooldown.get(player.getUniqueId()) - System.currentTimeMillis();
        return 0L;
    }

    /**
     * Add/remove (a) slot(s) to the player
     */
    private void addSlot() {
        stop();
        start();

        double count = Main.slots.getInt(player.getUniqueId() + "." + type) + getMultiplier();
        Main.slots.set(player.getUniqueId() + "." + type, count);
        save();

        // %slot%
        String slot;
        if (getMultiplier() > 1) {
            slot = "slots";
        } else {
            slot = "slot";
        }

        // %minute%
        String minute;
        if (Main.config.getInt("slot-cooldowns." + type) > 1) {
            minute = "minutes";
        } else {
            minute = "minute";
        }

        new MessageManager("slots.add")
                .replace("%count%", String.valueOf(getMultiplier()).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                .replace("%type%", type.substring(0, type.length() - 1))
                .replace("%slot%", slot)
                .replace("%total%", String.valueOf(count).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                .replace("%next%", String.valueOf(Main.config.getInt("slot-cooldowns." + type)))
                .replace("%minute%", minute)
                .send(player);
    }

    /**
     * Adds/removes slots when the cooldown reaches 0
     */
    public void check() {
        new BukkitRunnable() {
            public void run() {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    SlotManager slot = new SlotManager(type, online);
                    Essentials essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
                    if (slot.timeLeft() <= 0) {
                        // Check if Essentials is installed
                        if (essentials != null) {
                            // Check if player is AFK
                            if (essentials.getUser(online).isAfk()) {
                                slot.stop();
                                slot.start();
                                new MessageManager("slots.afk")
                                        .replace("%type%", type.substring(0, type.length() - 1))
                                        .send(online);
                            } else {
                                slot.addSlot();
                            }
                        } else {
                            slot.addSlot();
                        }
                    }
                }
            }
        }.runTaskTimer(Main.plugin, 0, 1);
    }

    /**
     * @return  The {@code player}'s slot multiplier <i>({@code vanadium.multiplier.X})</i>
     */
    private double getMultiplier() {
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String perm = pai.getPermission();
            if (perm.startsWith("vanadium.multiplier.")) {
                try {
                    return Double.parseDouble(perm.substring(perm.lastIndexOf(".") + 1)) / 100;
                } catch (NumberFormatException e) {
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
        return Main.slots.getInt(player.getUniqueId() + "." + type);
    }

    /**
     * Saves {@code slots.yml}
     */
    private void save() {
        new ConfigManager("slots.yml", true).saveData(Main.slots);
    }
}
