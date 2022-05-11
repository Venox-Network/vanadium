package xyz.srnyx.vanadium.managers;

import dev.lone.itemsadder.api.CustomStack;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import xyz.srnyx.vanadium.Main;

import org.apache.commons.lang.WordUtils;

import java.util.*;


public class LockManager {
    private Block block;
    private String name;
    private Player player;

    /**
     * Constructor for {@code LockManager}
     *
     * @param   block   The block to use for the methods
     * @param   player  The player to use for the methods
     */
    public LockManager(Block block, Player player) {
        if (block != null) {
            this.block = block;
            this.name = WordUtils.capitalizeFully(block.getType().name().replace("_", " "));
        }
        if (player != null) this.player = player;
    }

    /**
     * Checks if the block is locked
     *
     * @return  True if the block is locked, false if not
     */
    public boolean isLocked() {
        return DataManager.locked.containsKey(block) && getLocker() != null;
    }

    /**
     * Checks if the block is locked for the {@code player}
     *
     * @return  True if yes, false if no
     */
    public boolean isLockedForPlayer() {
        if (isLocked()) return !getLocker().equals(player.getUniqueId());
        return false;
    }

    /**
     * Gets the locker of the block
     *
     * @return  The locker
     */
    public UUID getLocker() {
        return DataManager.locked.get(block)[1];
    }

    /**
     * Checks if the block is in the {@code lockable-blocks} list
     *
     * @return  True if yes, false if no
     */
    public boolean isLockable() {
        for (String lockableBlock : Main.lists.getStringList("lockable-blocks")) {
            if (block.getType() == Material.getMaterial(lockableBlock.toUpperCase())) return true;
        }
        return false;
    }

    /**
     * Show the owned, trusted, and locked blocks around a player using particles
     */
    public void showLockedLocations() {
        List<Location> owned = new ArrayList<>();
        List<Location> trusted = new ArrayList<>();
        List<Location> locked = new ArrayList<>();
        for (Block key : DataManager.locked.keySet()) {
            if (key.getLocation().getWorld() == player.getLocation().getWorld() && key.getLocation().distance(player.getLocation()) <= 15) {
                UUID owner = new LockManager(key.getLocation().getBlock(), null).getLocker();
                if (owner != null) {
                    if (owner.equals(player.getUniqueId())) {
                        owned.add(key.getLocation());
                    } else if (new TrustManager(player, Bukkit.getOfflinePlayer(owner)).isTrusted()) {
                        trusted.add(key.getLocation());
                    } else {
                        locked.add(key.getLocation());
                    }
                }
            }
        }

        List<List<Location>> locations = Arrays.asList(owned, trusted, locked);
        particleCube(locations.get(0), Color.LIME, 0.25);
        particleCube(locations.get(1), Color.YELLOW, 0.25);
        particleCube(locations.get(2), Color.RED, 0.25);
    }

    /**
     * Shows a cube of particles for a block
     *
     * @param   locations   The locations to spawn particles at
     * @param   color       The color of the particles
     * @param   distance    The distance from the blocks to spawn particles (maybe)
     */
    public void particleCube(List<Location> locations, Color color, double distance) {
        List<Location> result = new ArrayList<>();
        for (Location loc : locations) {
            Location corner1 = loc.clone();
            Location corner2 = loc.clone().add(1, 1, 1);

            final double minX = Math.min(corner1.getX(), corner2.getX());
            final double minY = Math.min(corner1.getY(), corner2.getY());
            final double minZ = Math.min(corner1.getZ(), corner2.getZ());
            final double maxX = Math.max(corner1.getX(), corner2.getX());
            final double maxY = Math.max(corner1.getY(), corner2.getY());
            final double maxZ = Math.max(corner1.getZ(), corner2.getZ());

            for (double x = minX; x <= maxX; x += distance) {
                for (double y = minY; y <= maxY; y += distance) {
                    for (double z = minZ; z <= maxZ; z += distance) {
                        int components = 0;
                        if (x == minX || x == maxX) components++;
                        if (y == minY || y == maxY) components++;
                        if (z == minZ || z == maxZ) components++;
                        if (components >= 2) {
                            Location add = new Location(loc.getWorld(), x, y, z);
                            if (result.contains(add)) {
                                result.remove(add);
                            } else result.add(add);
                        }
                    }
                }
            }
        }
        Particle.DustOptions dust = new Particle.DustOptions(color, 0.7f);
        for (Location loc : result) player.spawnParticle(Particle.REDSTONE, loc, 0, 0, 0, 0, dust);
    }

    /**
     * Attempts to lock a block
     *
     * @param   item    The item to damage
     */
    public void attemptLock(ItemStack item) {

        // Check If Block Is Lockable
        if (!isLockable()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
            new MessageManager("locking.lock.invalid")
                    .replace("%block%", name)
                    .send(player);
            return;
        }

        // Check If Player Placed Block
        if (!Main.config.getBoolean("allow-unplaced-locking") || !PlayerManager.hasScoreboardTag(player, "bypass")) {
            UUID placer = new PlaceManager(block).getPlacer();
            if (placer == null || !placer.equals(player.getUniqueId())) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);

                String placerString = "N/A";
                if (placer != null) placerString = Bukkit.getOfflinePlayer(placer).getName();
                new MessageManager("locking.lock.fail")
                        .replace("%block%", name)
                        .replace("%player%", placerString)
                        .send(player);
                return;
            }
        } else new PlaceManager(block).attemptPlace(player);

        // Check If Block Is Already Locked
        if (isLocked()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
            new MessageManager("locking.lock.again")
                    .replace("%block%", name)
                    .replace("%player%", Bukkit.getOfflinePlayer(getLocker()).getName())
                    .send(player);
            return;
        }

        // Check if player has enough empty slots
        int slotCount = new SlotManager("locks", player).getCount();
        if (getLockedCount() >= slotCount) {
            new MessageManager("slots.limit")
                    .replace("%type%", "lock")
                    .replace("%target%", name)
                    .replace("%total%", String.valueOf(slotCount)) //.replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1")
                    .send(player);
            return;
        }

        // Attempt to lock double chest / door
        if (attemptLockDoubleChest(item) || attemptLockDoor(item)) return;

        if (!(block.getState() instanceof Chest chest && chest.getInventory() instanceof DoubleChestInventory) && !block.getType().toString().contains("_DOOR")) {
            lock(item);

            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
            new MessageManager("locking.lock.success")
                    .replace("%block%", name)
                    .send(player);
        }
    }

    /**
     * Locks a block
     *
     * @param   item    The item to damage
     */
    public void lock(ItemStack item) {
        if (item != null && item.getItemMeta() instanceof Damageable damage) {
            int current = damage.getDamage();
            if (current + 1 <= item.getType().getMaxDurability()) {
                damage.setDamage(current + 1);
                item.setItemMeta(damage);
            } else {
                player.playSound(player.getLocation(),Sound.ENTITY_ITEM_BREAK,1,1);
                item.setAmount(0);
            }
        }
        if (block != null) DataManager.locked.put(block, new UUID[]{new PlaceManager(block).getPlacer(), player.getUniqueId()});
    }

    /**
     * Attempts to lock a double chest
     *
     * @param   item    The item to damage
     *
     * @return          True if successful, false if not
     */
    public boolean attemptLockDoubleChest(ItemStack item) {
        // Check if block is a double chest
        if (block.getState() instanceof Chest chest && chest.getInventory() instanceof DoubleChestInventory doubleChest) {
            int slotCount = new SlotManager("locks", player).getCount();
            if (slotCount > getLockedCount() + 1) {
                for (Location location : new Location[]{doubleChest.getLeftSide().getLocation(), doubleChest.getRightSide().getLocation()}) {
                    new LockManager(location.getBlock(), player).lock(null);
                }
                new LockManager(null, player).lock(item);

                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
                new MessageManager("locking.lock.success")
                        .replace("%block%", name)
                        .send(player);
                return true;
            } else {
                new MessageManager("slots.limit")
                        .replace("%type%", "lock")
                        .replace("%target%", name)
                        .replace("%total%", String.valueOf(slotCount)) //.replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1")
                        .send(player);
            }
        }
        return false;
    }

    /**
     * Attempts to lock a door
     *
     * @return  True if successful, false if not
     */
    public boolean attemptLockDoor(ItemStack item) {
        if (block.getType().toString().contains("_DOOR")) {
            if (new SlotManager("locks", player).getCount() > (getLockedCount() + 1)) {
                //noinspection DuplicatedCode
                Door door = (Door) block.getState().getBlockData();
                Location top = block.getLocation();
                Location bottom = block.getLocation();

                if (door.getHalf() == Bisected.Half.TOP) bottom = block.getLocation().subtract(0, 1, 0);
                if (door.getHalf() == Bisected.Half.BOTTOM) top = block.getLocation().add(0, 1, 0);

                for (Location location : new Location[]{top, bottom}) new LockManager(location.getBlock(), player).lock(null);
                new LockManager(null, player).lock(item);

                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
                new MessageManager("locking.lock.success")
                        .replace("%block%", name)
                        .send(player);
                return true;
            } else {
                new MessageManager("slots.limit")
                        .replace("%type%", "lock")
                        .replace("%target%", name)
                        .replace("%total%", String.valueOf(DataManager.slots.get(player.getUniqueId())[0])) //.replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                        .send(player);
            }
        }
        return false;
    }

    /**
     * Checks if a double chest is locked
     */
    public void checkLockDoubleChest() {
        if (block.getState() instanceof Chest chest && chest.getInventory() instanceof DoubleChestInventory doubleChest) {
            location(doubleChest.getRightSide().getLocation(), doubleChest.getLeftSide().getLocation());
        }
    }

    /**
     * Checks if a door is locked
     */
    public void checkLockDoor() {
        if (block.getType().toString().contains("_DOOR")) {
            //noinspection DuplicatedCode
            Door door = (Door) block.getState().getBlockData();
            Location top = block.getLocation();
            Location bottom = block.getLocation();

            if (door.getHalf() == Bisected.Half.TOP) bottom = block.getLocation().subtract(0, 1, 0);
            if (door.getHalf() == Bisected.Half.BOTTOM) top = block.getLocation().add(0, 1, 0);

            location(top, bottom);
        }
    }

    /**
     * Locks 2 blocks, used for double chests and doors
     *
     * @param   one Location of the first block
     * @param   two Location of the second block
     */
    private void location(Location one, Location two) {
        Location[] locations = {one, two};
        for (Location loc : locations) {
            if (new LockManager(loc.getBlock(), null).isLocked()) {
                UUID owner = new LockManager(loc.getBlock(), null).getLocker();
                final Block block0 = locations[0].getBlock();
                final Block block1 = locations[0].getBlock();

                DataManager.locked.put(block0, new UUID[]{owner, owner});
                DataManager.locked.put(block1, new UUID[]{owner, owner});

                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
            }
        }
    }

    /**
     * Attempts to unlock a block
     *
     * @param   item    The item to repair
     */
    public void attemptUnlock(ItemStack item) {

        // Check If Block Is Locked
        if (!isLocked()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
            new MessageManager("locking.unlock.invalid")
                    .replace("%block%", name)
                    .send(player);
            return;
        }

        // Check If Player Placed Block
        UUID placer = new PlaceManager(block).getPlacer();
        if ((placer == null || !placer.equals(player.getUniqueId())) && !player.hasPermission("vanadium.command.bypass")) {
            String username = "N/A";
            if (placer != null) username = Bukkit.getOfflinePlayer(placer).getName();
            new MessageManager("locking.unlock.fail")
                    .replace("%block%", name)
                    .replace("%player%", username)
                    .send(player);

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
            return;
        }

        // Attempt to unlock double chest / door
        if (attemptUnlockDoubleChest(item) || attemptUnlockDoor(item)) return;

        if (!(block.getState() instanceof Chest chest && chest.getInventory() instanceof DoubleChestInventory) && !block.getType().toString().contains("_DOOR")) {
            unlock(item);

            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
            new MessageManager("locking.unlock.success")
                    .replace("%block%", name)
                    .send(player);
        }
    }

    /**
     * Unlocks a block
     *
     * @param   item    The item to repair
     */
    public void unlock(ItemStack item) {
        if (item != null && item.getItemMeta() instanceof Damageable damage) {
            if (damage.getDamage() - 1 <= item.getType().getMaxDurability()) {
                damage.setDamage(damage.getDamage() - 1);
                item.setItemMeta(damage);
            } else {
                player.playSound(player.getLocation(),Sound.ENTITY_ITEM_BREAK,1,1);
                item.setAmount(0);
            }
        }
        if (block != null) DataManager.locked.put(block, new UUID[]{new PlaceManager(block).getPlacer(), null});
    }

    /**
     * Attempts to unlock a double chest
     *
     * @param   item    The item to repair
     *
     * @return          True if unlock was successful, false if not
     */
    public boolean attemptUnlockDoubleChest(ItemStack item) {
        if (block.getState() instanceof Chest chest && chest.getInventory() instanceof DoubleChestInventory doubleChest) {
            attemptUnlockDouble(item, doubleChest.getLeftSide().getLocation(), doubleChest.getRightSide().getLocation());
            return true;
        }
        return false;
    }

    /**
     * Attempts to unlock a door
     *
     * @param   item    The item to repair
     *
     * @return          True if unlock was successful, false if not
     */
    public boolean attemptUnlockDoor(ItemStack item) {
        if (block.getType().toString().contains("_DOOR")) {
            //noinspection DuplicatedCode
            Door door = (Door) block.getState().getBlockData();
            Location top = block.getLocation();
            Location bottom = block.getLocation();

            if (door.getHalf() == Bisected.Half.TOP) bottom = block.getLocation().subtract(0, 1, 0);
            if (door.getHalf() == Bisected.Half.BOTTOM) top = block.getLocation().add(0, 1, 0);

            attemptUnlockDouble(item, top, bottom);
            return true;
        }
        return false;
    }

    /**
     * Used to unlock a double chest or door
     *
     * @param   item    The item to repair
     * @param   one     The first block to unlock
     * @param   two     The second block to unlock
     */
    private void attemptUnlockDouble(ItemStack item, Location one, Location two) {
        for (Location location : new Location[]{one, two}) new LockManager(location.getBlock(), player).unlock(null);
        new LockManager(null, player).unlock(item);

        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
        new MessageManager("locking.unlock.success")
                .replace("%block%", name)
                .send(player);
    }

    /**
     * Get the current number of blocks {@code player} has locked
     *
     * @return  How many blocks {@code player} currently has locked
     */
    public int getLockedCount() {
        int i = 0;
        for (Block key : DataManager.locked.keySet()) if (Objects.equals(new LockManager(key, null).getLocker(), player.getUniqueId())) i++;
        return i;
    }

    /**
     * @return  Lock Tool as an {@code ItemStack}
     */
    public static ItemStack getLockTool() {
        if (Main.config.getBoolean("lock-tool.custom")) {
            return CustomStack.getInstance("vanadium:lock_tool").getItemStack();
        } else {
            return new ItemManager(Material.GOLDEN_HOE)
                    .name("&3Lock Tool")
                    .lore("&7Left-click &b= &7Lock")
                    .lore("&7Right-click &b= &7Unlock")
                    .enchant(Enchantment.WATER_WORKER, 1)
                    .flag(ItemFlag.HIDE_ENCHANTS)
                    .get();
        }
    }

    /**
     * Checks if a player is holding a Lock Tool
     *
     * @return  True if holding, false if not
     */
    public boolean holdingLockTool() {
        return ItemManager.isSame(player.getInventory().getItemInMainHand(), getLockTool());
    }

    /**
     * Checks if locked block types are still the same as the saved types
     */
    public void check() {
        for (Block key : DataManager.locked.keySet()) {
            if (!key.getType().equals(DataManager.lockedType.get(key))) {
                DataManager.locked.remove(key);
                DataManager.lockedType.remove(key);
            }
        }
    }
}
