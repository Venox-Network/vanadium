package network.venox.vanadium.listeners;

import network.venox.vanadium.managers.LockManager;
import network.venox.vanadium.managers.MessageManager;
import network.venox.vanadium.managers.PlayerManager;
import network.venox.vanadium.managers.TrustManager;

import org.apache.commons.lang3.text.WordUtils;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.UUID;


public class InteractListener implements Listener {
    /**
     * Called when a player interacts
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();

        // If Holding Lock Tool
        if (new LockManager(null, player).holdingLockTool() && event.getHand() == EquipmentSlot.HAND && block != null) {
            // Lock
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                new LockManager(block, player).attemptLock(player.getInventory().getItemInMainHand());
                event.setCancelled(true);
            }

            // Unlock
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                new LockManager(block, player).attemptUnlock(player.getInventory().getItemInMainHand());
                event.setCancelled(true);
            }
        }

        // Check if locked
        if (block != null && new LockManager(block, player).isLockedForPlayer()) {
            final UUID owner = new LockManager(block, null).getLocker();
            if (owner == null || new TrustManager(player, Bukkit.getOfflinePlayer(owner)).isTrusted(block)) return;

            if (!(player.hasPermission("vanadium.command.bypass") && (player.isSneaking() || PlayerManager.hasScoreboardTag(player, "bypass")))) {
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 1);
                event.setCancelled(true);
            }

            new MessageManager("locking.block-locked")
                    .replace("%block%", WordUtils.capitalizeFully(block.getType().name().replace("_", " ")))
                    .replace("%player%", Bukkit.getOfflinePlayer(new LockManager(block, null).getLocker()).getName())
                    .send(player);
        }
    }

    /**
     * Called when an entity interacts
     */
    @EventHandler
    public void onEntityInteract(EntityInteractEvent event) {
        // Cancel if entity interacts with locked blocked
        if (new LockManager(event.getBlock(), null).isLocked()) event.setCancelled(true);
    }
}
