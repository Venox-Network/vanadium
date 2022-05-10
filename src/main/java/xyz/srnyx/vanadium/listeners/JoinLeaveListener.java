package xyz.srnyx.vanadium.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import xyz.srnyx.vanadium.commands.CommandBypass;
import xyz.srnyx.vanadium.commands.CommandSlot;
import xyz.srnyx.vanadium.managers.PlayerManager;
import xyz.srnyx.vanadium.managers.SlotManager;


public class JoinLeaveListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Enable bypass
        if (PlayerManager.hasScoreboardTag(player, "bypass")) CommandBypass.enable(player, true);

        // Start slot cooldown
        if (!CommandSlot.stopLocks.contains(player.getUniqueId())) new SlotManager("locks", player).start();
        if (!CommandSlot.stopTrusts.contains(player.getUniqueId())) new SlotManager("trusts", player).start();
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        // Stop slot cooldown
        new SlotManager("locks", event.getPlayer()).stop();
        new SlotManager("trusts", event.getPlayer()).stop();
    }
}
