package xyz.srnyx.vanadium.listeners;

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
import org.bukkit.inventory.ItemStack;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.commands.CommandBypass;
import xyz.srnyx.vanadium.managers.*;

import java.util.UUID;


public class InteractListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack item = player.getInventory().getItemInMainHand();

        // If Holding Lock Tool
        if (new LockManager().holdingLockTool(player) && event.getHand() == EquipmentSlot.HAND && block != null) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                new LockManager(block).attemptLock(player, item);
                event.setCancelled(true);
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                new LockManager(block).attemptUnlock(player, item);
                event.setCancelled(true);
            }
        }

        // Check if locked
        if (block != null && new LockManager(block).isLockedForPlayer(player)) {
            String owner = Main.locked.getString(new LockManager(block).getId() + ".locked");
            if (owner != null && !new TrustManager(Bukkit.getOfflinePlayer(UUID.fromString(owner)), player).isTrusted()) {
                if (!(player.hasPermission("vanadium.command.bypass") && (player.isSneaking() || CommandBypass.check(player)))) {
                    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 1);
                    event.setCancelled(true);
                }
                new MessageManager("locking.block-locked")
                        .replace("%block%", new LockManager(block).getName())
                        .replace("%player%", Bukkit.getOfflinePlayer(UUID.fromString(new LockManager(block).getLocker())).getName())
                        .send(player);
            }
        }
    }

    @EventHandler
    public void onEntityInteract(EntityInteractEvent event) {
        // Cancel if entity interacts with locked blocked (Main.locked.getString(new LockManager(event.getBlock()).getId() + ".locked") != null)
        if (new LockManager(event.getBlock()).isLocked()) {
            event.setCancelled(true);
            Bukkit.broadcastMessage(event.getEntity().getName() + " just triggered " + event.getBlock() + " at " + event.getBlock().getLocation());
        }
    }
}
