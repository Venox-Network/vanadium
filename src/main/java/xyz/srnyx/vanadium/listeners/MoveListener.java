package xyz.srnyx.vanadium.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.managers.LockManager;


public class MoveListener implements Listener {
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking() && new LockManager(null, player).holdingLockTool()) {
            new BukkitRunnable() {
                public void run() {
                    new LockManager(null, player).showLockedLocations();
                    if (!player.isSneaking()) {
                        cancel();
                    }
                }
            }.runTaskTimer(Main.plugin, 1, 5);
        }
    }
}
