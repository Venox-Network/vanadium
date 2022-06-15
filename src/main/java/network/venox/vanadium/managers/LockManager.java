package network.venox.vanadium.managers;

import dev.lone.itemsadder.api.CustomStack;

import network.venox.vanadium.Main;
import network.venox.vanadium.managers.slots.LockSlotManager;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

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
        for (final String lockableBlock : Main.lists.getStringList("lockable-blocks")) {
            if (block.getType() == Material.getMaterial(lockableBlock.toUpperCase())) return true;
        }
        return false;
    }

    /**
     * Show the owned, trusted, and locked blocks around a player using particles
     */
    public void showLockedLocations() {
        final List<Location> owned = new ArrayList<>();
        final List<Location> trusted = new ArrayList<>();
        final List<Location> locked = new ArrayList<>();
        for (final Block key : DataManager.locked.keySet()) {
            if (key.getLocation().getWorld() == player.getLocation().getWorld() && key.getLocation().distance(player.getLocation()) <= 15) {
                final UUID owner = new LockManager(key.getLocation().getBlock(), null).getLocker();
                if (owner != null) {
                    if (owner.equals(player.getUniqueId())) {
                        owned.add(key.getLocation());
                    } else if (new TrustManager(player, Bukkit.getOfflinePlayer(owner)).isTrusted(key)) {
                        trusted.add(key.getLocation());
                    } else locked.add(key.getLocation());
                }
            }
        }

        final List<List<Location>> locations = Arrays.asList(owned, trusted, locked);
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
        final List<Location> result = new ArrayList<>();
        for (final Location loc : locations) {
            final Location corner1 = loc.clone();
            final Location corner2 = loc.clone().add(1, 1, 1);

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
                            final Location add = new Location(loc.getWorld(), x, y, z);
                            if (result.contains(add)) {
                                result.remove(add);
                            } else result.add(add);
                        }
                    }
                }
            }
        }
        for (final Location loc : result) player.spawnParticle(Particle.REDSTONE, loc, 0, 0, 0, 0, new Particle.DustOptions(color, 0.7f));
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
            final UUID placer = new PlaceManager(block).getPlacer();
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
        final int slotCount = new LockSlotManager().getCount(player.getUniqueId());
        if (getLockedCount() >= slotCount) {
            new MessageManager("slots.limit")
                    .replace("%type%", "lock")
                    .replace("%target%", name)
                    .replace("%total%", String.valueOf(slotCount)) //.replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1")
                    .send(player);
            return;
        }

        // Attempt lock double chest
        if (block.getState() instanceof Chest chest && chest.getInventory() instanceof DoubleChestInventory && attemptLockDoubleChest(item)) return;

        // Attempt lock door
        if (block.getType().toString().contains("_DOOR") && attemptLockDoor(item)) return;

        // Lock block
        lock(item, 1);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
        new MessageManager("locking.lock.success")
                .replace("%block%", name)
                .send(player);
    }

    /**
     * Locks a block
     *
     * @param   item    The item to damage
     */
    public void lock(ItemStack item, Integer dmg) {
        if (item != null) {
            if (Main.itemsAdderInstalled()) {
                // ItemsAdder
                final CustomStack custom = CustomStack.byItemStack(item);
                if (custom != null && Objects.equals(custom.getId(), "lock_tool")) {
                    final int newDmg = custom.getDurability() - dmg;
                    custom.setDurability(custom.getDurability() - dmg);

                    // Remove item if new durability is 0
                    if (newDmg <= 0) {
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                        item.setAmount(0);
                    }
                }
            } else if (item.getItemMeta() instanceof Damageable damageable) {
                // Vanilla
                int newDmg = damageable.getDamage() + dmg;
                if (damageable.getDamage() == 0) newDmg = dmg;

                damageable.setDamage(newDmg);
                item.setItemMeta(damageable);

                // Remove item if new durability is 0
                if (newDmg >= item.getType().getMaxDurability()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                    item.setAmount(0);
                }
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
        final Chest chest = (Chest) block.getState();
        final DoubleChestInventory doubleChest = (DoubleChestInventory) chest.getInventory();

        final int slotCount = new LockSlotManager().getCount(player.getUniqueId());
        if (slotCount <= getLockedCount() + 1) {
            new MessageManager("slots.limit")
                    .replace("%type%", "lock")
                    .replace("%target%", name)
                    .replace("%total%", String.valueOf(slotCount)) //.replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1")
                    .send(player);
            return false;
        }

        for (final Location location : new Location[]{doubleChest.getLeftSide().getLocation(), doubleChest.getRightSide().getLocation()}) {
            new LockManager(location.getBlock(), player).lock(item, 1);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
        new MessageManager("locking.lock.success")
                .replace("%block%", name)
                .send(player);
        return true;
    }

    /**
     * Attempts to lock a door
     *
     * @return  True if successful, false if not
     */
    public boolean attemptLockDoor(ItemStack item) {
        if (block.getType().toString().contains("_DOOR")) {
            if (new LockSlotManager().getCount(player.getUniqueId()) <= (getLockedCount() + 1)) {
                new MessageManager("slots.limit")
                        .replace("%type%", "lock")
                        .replace("%target%", name)
                        .replace("%total%", String.valueOf(DataManager.slots.get(player.getUniqueId())[0])) //.replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                        .send(player);
                return false;
            }

            for (final Location location : Main.door(block)) new LockManager(location.getBlock(), player).lock(null, null);
            lock(item, 1);

            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
            new MessageManager("locking.lock.success")
                    .replace("%block%", name)
                    .send(player);
            return true;
        }
        return false;
    }

    /**
     * Locks 2 blocks, used for double chests and doors
     *
     * @param   one Location of the first block
     * @param   two Location of the second block
     */
    public void doubleLock(Location one, Location two) {
        final Location[] locations = {one, two};
        for (final Location loc : locations) {
            final LockManager lock = new LockManager(loc.getBlock(), null);
            if (!lock.isLocked()) return;

            final UUID owner = lock.getLocker();
            final Block block0 = locations[0].getBlock();
            final Block block1 = locations[0].getBlock();

            DataManager.locked.put(block0, new UUID[]{owner, owner});
            DataManager.locked.put(block1, new UUID[]{owner, owner});

            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
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
        final UUID placer = new PlaceManager(block).getPlacer();
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
            unlock(item, 1);

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
    public void unlock(ItemStack item, Integer repair) {
        if (item != null) {
            if (Main.itemsAdderInstalled()) {
                // ItemsAdder
                final CustomStack custom = CustomStack.byItemStack(item);
                if (custom != null && Objects.equals(custom.getId(), "lock_tool") && custom.getDurability() + repair <= custom.getMaxDurability()) {
                    custom.setDurability(custom.getDurability() + repair);
                }
            } else if (item.getItemMeta() instanceof Damageable damage && damage.getDamage() - repair <= item.getType().getMaxDurability()) {
                damage.setDamage(damage.getDamage() - repair);
                item.setItemMeta(damage);
            }
        }

        if (block == null) return;
        DataManager.locked.put(block, new UUID[]{new PlaceManager(block).getPlacer(), null});
        DataManager.lockedTrusted.remove(block);
    }

    /**
     * Attempts to unlock a double chest
     *
     * @param   item    The item to repair
     *
     * @return          True if unlock was successful, false if not
     */
    public boolean attemptUnlockDoubleChest(ItemStack item) {
        if (!(block.getState() instanceof Chest chest) || !(chest.getInventory() instanceof DoubleChestInventory doubleChest)) return false;
        attemptUnlockDouble(item, 2, doubleChest.getLeftSide().getLocation(), doubleChest.getRightSide().getLocation());
        return true;
    }

    /**
     * Attempts to unlock a door
     *
     * @param   item    The item to repair
     *
     * @return          True if unlock was successful, false if not
     */
    public boolean attemptUnlockDoor(ItemStack item) {
        if (!block.getType().toString().contains("_DOOR")) return false;
        attemptUnlockDouble(item, 1, Main.door(block)[0], Main.door(block)[1]);
        return true;
    }

    /**
     * Used to unlock a double chest or door
     *
     * @param   item    The item to repair
     * @param   one     The first block to unlock
     * @param   two     The second block to unlock
     */
    private void attemptUnlockDouble(ItemStack item, int dmg, Location one, Location two) {
        for (final Location location : new Location[]{one, two}) new LockManager(location.getBlock(), player).unlock(null, null);
        unlock(item, dmg);

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
        for (final Block key : DataManager.locked.keySet()) if (Objects.equals(new LockManager(key, null).getLocker(), player.getUniqueId())) i++;
        return i;
    }

    /**
     * @return  Lock Tool as an {@code ItemStack}
     */
    public static ItemStack getLockTool() {
        if (Main.config.getBoolean("lock-tool.custom")) return CustomStack.getInstance("vanadium:lock_tool").getItemStack();

        return new ItemManager(Material.GOLDEN_HOE)
                .name("&3Lock Tool")
                .lore("&7Left-click &b= &7Lock")
                .lore("&7Right-click &b= &7Unlock")
                .enchant(Enchantment.WATER_WORKER, 1)
                .flag(ItemFlag.HIDE_ENCHANTS)
                .get();
    }

    /**
     * Checks if a player is holding a Lock Tool
     *
     * @return  True if holding, false if not
     */
    public boolean holdingLockTool() {
        final ItemStack item = player.getInventory().getItemInMainHand();

        // ItemsAdder
        if (Main.itemsAdderInstalled()) return CustomStack.byItemStack(item) != null && Objects.equals(CustomStack.byItemStack(item).getId(), "lock_tool");

        // Vanilla
        final ItemMeta oneMeta = item.getItemMeta();
        final ItemMeta twoMeta = getLockTool().getItemMeta();
        if (oneMeta == null || twoMeta == null) return false;

        final boolean type = Objects.equals(item.getType(), getLockTool().getType());
        final boolean displayName = Objects.equals(oneMeta.getDisplayName(), twoMeta.getDisplayName());
        final boolean lore = Objects.equals(oneMeta.getLore(), twoMeta.getLore());
        return type && displayName && lore;
    }
}
