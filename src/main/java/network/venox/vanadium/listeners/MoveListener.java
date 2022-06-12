package network.venox.vanadium.listeners;

import network.venox.vanadium.Main;
import network.venox.vanadium.managers.LockManager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;


public class MoveListener implements Listener {
    /**
     * Called when a player sneaks
     */
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        final Player player = event.getPlayer();
        if (player.isSneaking() || !new LockManager(null, player).holdingLockTool()) return;
        new BukkitRunnable() {public void run() {
            new LockManager(null, player).showLockedLocations();
            if (!player.isSneaking()) cancel();
        }}.runTaskTimer(Main.plugin, 1, 5);
    }
}
