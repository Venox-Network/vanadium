package network.venox.vanadium.listeners;

import network.venox.vanadium.managers.BypassManager;
import network.venox.vanadium.managers.PlayerManager;
import network.venox.vanadium.managers.slots.LockSlotManager;
import network.venox.vanadium.managers.slots.TrustSlotManager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class JoinLeaveListener implements Listener {
    /**
     * Called when a player joins
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        // Enable bypass
        if (PlayerManager.hasScoreboardTag(player, "bypass")) new BypassManager().enable(player, true);

        // Start slot cooldown
        if (!PlayerManager.hasScoreboardTag(player, "stop-locks")) new LockSlotManager(player).start();
        if (!PlayerManager.hasScoreboardTag(player, "stop-trusts")) new TrustSlotManager(player).start();
    }

    /**
     * Called when a player quits
     */
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        // Stop slot cooldown
        new LockSlotManager(event.getPlayer()).stop();
        new TrustSlotManager(event.getPlayer()).stop();
    }
}
