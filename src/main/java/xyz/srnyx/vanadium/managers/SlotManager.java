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
    private String type;

    private static final Map<UUID, Long> locksCooldown = new HashMap<>();
    private static final Map<UUID, Long> trustsCooldown = new HashMap<>();

    public SlotManager(String type, Player player) {
        this.type = type;
        this.player = player;
    }

    public SlotManager(String type) {
        this.type = type;
    }

    public SlotManager() {}

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
        if (Objects.equals(type, "locks")) locksCooldown.remove(player.getUniqueId());
        if (Objects.equals(type, "trusts")) trustsCooldown.remove(player.getUniqueId());
    }

    /**
     * @return  The amount of time left in the cooldown
     */
    public Long timeLeft(boolean papi) {
        if (new SlotManager(type, player).contains()) {
            if (Objects.equals(type, "locks")) return locksCooldown.get(player.getUniqueId()) - System.currentTimeMillis();
            if (Objects.equals(type, "trusts")) return trustsCooldown.get(player.getUniqueId()) - System.currentTimeMillis();
        }
        if (papi) return null;
        return 0L;
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

        double count = Main.slots.getInt(player.getUniqueId() + "." + type) + getMultiplier();
        Main.slots.set(player.getUniqueId() + "." + type, count);
        save();

        // %slot%
        String slot;
        if (getMultiplier() != 1) {
            slot = "slots";
        } else slot = "slot";

        // %minute%
        String minute;
        if (Main.config.getInt("slot-cooldowns." + type) > 1) {
            minute = "minutes";
        } else minute = "minute";

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
                    if (slot.contains() && slot.timeLeft(false) <= 0) {
                        Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                        if (essentials != null) {
                            if (essentials.getUser(online).isAfk()) {
                                slot.stop();
                                slot.start();
                            } else slot.addSlot();
                        } else slot.addSlot();
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
                    return Double.parseDouble(perm.substring(perm.lastIndexOf(".") + 1));
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
    public void save() {
        new ConfigManager("slots.yml", true).saveData(Main.slots);
    }
}
