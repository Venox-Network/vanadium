package network.venox.vanadium.listeners;

import network.venox.vanadium.Main;
import network.venox.vanadium.managers.LockManager;
import network.venox.vanadium.managers.MessageManager;
import network.venox.vanadium.managers.PlaceManager;
import network.venox.vanadium.managers.PlayerManager;

import org.apache.commons.lang.WordUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;


public class BlockListener implements Listener {
    /**
     * Called when a block is placed
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        // Check if it should log placer
        if (new LockManager(block, null).isLockable()) new PlaceManager(block).attemptPlace(player);

        // Check if holding lock tool
        if (new LockManager(null, player).holdingLockTool()) event.setCancelled(true);

        // Check if locked double chest or door
        new BukkitRunnable() {
            public void run() {
                final LockManager lock = new LockManager(block, player);
                lock.checkLockDoubleChest();
                lock.checkLockDoor();
            }
        }.runTaskLater(Main.plugin, 1);
    }

    /**
     * Called when a block is broken
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        // Check Holding Lock Tool
        if (new LockManager(null, player).holdingLockTool()) {
            event.setCancelled(true);
            return;
        }

        // Check Locked Block
        final boolean bypass = player.hasPermission("vanadium.command.bypass") && (player.isSneaking() || PlayerManager.hasScoreboardTag(player, "bypass"));
        if (!bypass && new LockManager(block, null).isLocked()) {
            if (new LockManager(block, player).isLockedForPlayer()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 1);
                new MessageManager("locking.block-locked")
                        .replace("%block%", WordUtils.capitalizeFully(block.getType().name().replace("_", " ")))
                        .replace("%player%", Bukkit.getOfflinePlayer(new LockManager(block, null).getLocker()).getName())
                        .send(player);
                return;
            }

            new LockManager(block, player).attemptUnlock(null);
        }

        // Check if Should Remove Placer
        if (new PlaceManager(block).isPlaced()) new PlaceManager(block).attemptUnplace();
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
    public void onBurn(BlockBurnEvent event) {
        if (new LockManager(event.getBlock(), null).isLocked()) event.setCancelled(true);
    }

    /**
     * Called when an item is moved from one inventory to another
     */
    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event) {
        final Location source = event.getSource().getLocation();
        final Inventory destination = event.getDestination();
        if (source == null || destination.getLocation() == null || destination.getType() != InventoryType.HOPPER) return;

        final UUID sourceOwner = new LockManager(source.getBlock(), null).getLocker();
        final UUID destinationOwner = new LockManager(destination.getLocation().getBlock(), null).getLocker();
        if ((sourceOwner == null || destinationOwner != null) && (sourceOwner == null || destinationOwner.equals(sourceOwner))) return;

        event.setCancelled(true);
    }
}
