package network.venox.vanadium.managers.slots;

import network.venox.vanadium.Main;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;


public class SlotManager {
    /**
     * Runs {@link LockSlotManager#check} and {@link TrustSlotManager#check} for every online player every tick
     */
    public void runCheck() {
        new BukkitRunnable() {
            public void run() {
                for (final Player online : Bukkit.getOnlinePlayers()) {
                    new LockSlotManager(online).check();
                    new TrustSlotManager(online).check();
                }
            }
        }.runTaskTimer(Main.plugin, 0, 1);
    }

    /**
     * @return  The {@code player}'s slot multiplier <i>({@code vanadium.multiplier.X})</i>
     */
    public int getMultiplier(Player player) {
        for (final PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            final String perm = pai.getPermission();
            if (perm.startsWith("vanadium.multiplier.")) return Integer.parseInt(perm.substring(perm.lastIndexOf('.') + 1));
        }
        return 1;
    }

    /**
     * Used for dynamic stuff
     *
     * @param   player  The player involved in the action
     * @param   type    The type of slot
     *
     * @return          The time left in the cooldown
     */
    public Long timeLeft(Player player, String type) {
        if (type.equalsIgnoreCase("locks")) return new LockSlotManager(player).timeLeft();
        if (type.equalsIgnoreCase("trusts")) return new TrustSlotManager(player).timeLeft();
        return 0L;
    }

    /**
     * Used for dynamic stuff
     *
     * @param   player  The player involved in the action
     * @param   type    The type of slot
     */
    public void start(Player player, String type) {
        if (type.equalsIgnoreCase("locks")) new LockSlotManager(player).start();
        if (type.equalsIgnoreCase("trusts")) new TrustSlotManager(player).start();
    }

    /**
     * Used for dynamic stuff
     *
     * @param   player  The player involved in the action
     * @param   type    The type of slot
     */
    public void stop(Player player, String type) {
        if (type.equalsIgnoreCase("locks")) new LockSlotManager(player).stop();
        if (type.equalsIgnoreCase("trusts")) new TrustSlotManager(player).stop();
    }

    /**
     * Used for CommandSlot#onCommand
     *
     * @param   player  The player involved in the action
     * @param   type    The type of slot
     * @param   action  The action to perform
     */
    public void startStop(Player player, String type, String action) {
        if (action.equalsIgnoreCase("start")) {
            if (type.equalsIgnoreCase("all")) {
                new LockSlotManager(player).start();
                new TrustSlotManager(player).start();
                player.removeScoreboardTag("stop-locks");
                player.removeScoreboardTag("stop-trusts");
                return;
            }
            
            start(player, type);
            player.removeScoreboardTag("stop-" + type);
        }

        if (action.equalsIgnoreCase("stop")) {
            if (type.equalsIgnoreCase("all")) {
                new LockSlotManager(player).stop();
                new TrustSlotManager(player).stop();
                player.addScoreboardTag("stop-locks");
                player.addScoreboardTag("stop-trusts");
                return;
            }

            stop(player, type);
            player.addScoreboardTag("stop-" + type);
        }
    }
}
