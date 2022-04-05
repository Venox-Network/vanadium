package xyz.srnyx.vanadium.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scheduler.BukkitRunnable;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.commands.CommandBypass;
import xyz.srnyx.vanadium.managers.LockManager;
import xyz.srnyx.vanadium.managers.MessageManager;

import java.util.UUID;


public class BlockListener implements Listener {

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Check if Should Log Placer
        if (new LockManager(block).isLockable()) {
            new LockManager(block).attemptPlace(player);
        }

        // Check Holding Lock Tool
        if (new LockManager().holdingLockTool(player)) {
            event.setCancelled(true);
        }

        // Check if Locked double chest or door
        new BukkitRunnable() {
            public void run() {
                new LockManager(block).checkLockDoubleChest(player);
                new LockManager(block).checkLockDoor(player);
            }
        }.runTaskLater(Main.plugin, 1);
    }

    @EventHandler
    public void onDestroyBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Check Holding Lock Tool
        if (new LockManager().holdingLockTool(player)) {
            event.setCancelled(true);
            return;
        }

        // Check Locked Block
        if (!(player.hasPermission("vanadium.command.bypass") && (player.isSneaking() || CommandBypass.check(player))) && new LockManager(block).isLocked()) {
            if (new LockManager(block).isLockedForPlayer(player)) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 1);
                new MessageManager("locking.block-locked")
                        .replace("%block%", new LockManager(block).getName())
                        .replace("%player%", Bukkit.getOfflinePlayer(UUID.fromString(new LockManager(block).getLocker())).getName())
                        .send(player);
                return;
            } else {
                new LockManager(block).attemptUnlock(player, null);
            }
        }

        // Check if Should Remove Placer
        if (new LockManager(block).isPlaced()) {
            new LockManager(block).attemptUnplace();
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> new LockManager(block).isLocked());
    }

    @EventHandler
    public void onFire(BlockBurnEvent event) {
        Block block = event.getBlock();
        if (new LockManager(block).isLocked()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHopper(InventoryMoveItemEvent event) {
        if (event.getSource().getLocation() != null && event.getDestination().getLocation() != null && event.getDestination().getType() == InventoryType.HOPPER) {
            String sourceOwner = new LockManager(event.getSource().getLocation().getBlock()).getLocker();
            String destinationOwner = new LockManager(event.getDestination().getLocation().getBlock()).getLocker();
            if (sourceOwner != null && destinationOwner == null || sourceOwner != null && !destinationOwner.equals(sourceOwner)) {
                event.setCancelled(true);
            }
        }
    }
}
