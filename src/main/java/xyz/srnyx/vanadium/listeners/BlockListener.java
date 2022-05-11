package xyz.srnyx.vanadium.listeners;

import org.apache.commons.lang.WordUtils;
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
import xyz.srnyx.vanadium.managers.LockManager;
import xyz.srnyx.vanadium.managers.MessageManager;
import xyz.srnyx.vanadium.managers.PlaceManager;
import xyz.srnyx.vanadium.managers.PlayerManager;

import java.util.UUID;


public class BlockListener implements Listener {
    /**
     * Called when a block is placed
     */
    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Check if Should Log Placer
        if (new LockManager(block, null).isLockable()) {
            new PlaceManager(block).attemptPlace(player);
        }

        // Check Holding Lock Tool
        if (new LockManager(null, player).holdingLockTool()) {
            event.setCancelled(true);
        }

        // Check if Locked double chest or door
        new BukkitRunnable() {
            public void run() {
                new LockManager(block, player).checkLockDoubleChest();
                new LockManager(block, player).checkLockDoor();
            }
        }.runTaskLater(Main.plugin, 1);
    }

    /**
     * Called when a block is broken
     */
    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Check Holding Lock Tool
        if (new LockManager(null, player).holdingLockTool()) {
            event.setCancelled(true);
            return;
        }

        // Check Locked Block
        if (!(player.hasPermission("vanadium.command.bypass") && (player.isSneaking() || PlayerManager.hasScoreboardTag(player, "bypass"))) && new LockManager(block, null).isLocked()) {
            if (new LockManager(block, player).isLockedForPlayer()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 1);
                new MessageManager("locking.block-locked")
                        .replace("%block%", WordUtils.capitalizeFully(block.getType().name().replace("_", " ")))
                        .replace("%player%", Bukkit.getOfflinePlayer(new LockManager(block, null).getLocker()).getName())
                        .send(player);
                return;
            } else {
                new LockManager(block, player).attemptUnlock(null);
            }
        }

        // Check if Should Remove Placer
        if (new PlaceManager(block).isPlaced()) {
            new PlaceManager(block).attemptUnplace();
        }
    }

    /**
     * Called when an entity explodes
     */
    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> new LockManager(block, null).isLocked());
    }

    /**
     * Called when a block burns
     */
    @EventHandler
    public void onFire(BlockBurnEvent event) {
        Block block = event.getBlock();
        if (new LockManager(block, null).isLocked()) {
            event.setCancelled(true);
        }
    }

    /**
     * Called when an item is moved from one inventory to another
     */
    @EventHandler
    public void onHopper(InventoryMoveItemEvent event) {
        if (event.getSource().getLocation() != null && event.getDestination().getLocation() != null && event.getDestination().getType() == InventoryType.HOPPER) {
            UUID sourceOwner = new LockManager(event.getSource().getLocation().getBlock(), null).getLocker();
            UUID destinationOwner = new LockManager(event.getDestination().getLocation().getBlock(), null).getLocker();
            if (sourceOwner != null && destinationOwner == null || sourceOwner != null && !destinationOwner.equals(sourceOwner)) {
                event.setCancelled(true);
            }
        }
    }
}
