package xyz.srnyx.vanadium.managers;

import dev.lone.itemsadder.api.CustomStack;

import org.bukkit.Sound;
import org.bukkit.Color;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.Damageable;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.commands.CommandBypass;

import org.apache.commons.lang.WordUtils;

import java.util.*;


public class LockManager {
    @SuppressWarnings({"CanBeFinal"})
    private Block block;

    /**
     * Constructor for {@code LockManager}
     *
     * @param   block   The block to use for the methods
     */
    public LockManager(Block block) {
        this.block = block;

    }

    /**
     * Constructor for {@code LockManager} without parameters
     */
    public LockManager() {}

    /**
     * Checks if the block is locked
     *
     * @return  True if the block is locked, false if not
     */
    public boolean isLocked() {
        return Main.locked.contains(getId() + ".locker");
    }

    /**
     * Checks if the block is locked for the {@code player}
     *
     * @param   player  The player to check for
     * @return          True if yes, false if no
     */
    public boolean isLockedForPlayer(Player player) {
        if (isLocked()) {
            String owner = getLocker();
            if (owner != null) {
                return !owner.equalsIgnoreCase(player.getUniqueId().toString());
            }
        }
        return false;
    }

    /**
     * Gets the locker of the block
     *
     * @return  The locker
     */
    public String getLocker() {
        if (isLocked()) {
            return Main.locked.getString(getId() + ".locker");
        }
        return null;
    }

    /**
     * Checks if the block is in the {@code lockable-blocks} list
     *
     * @return  True if yes, false if no
     */
    public boolean isLockable() {
        for (String lockableBlock : Main.lists.getStringList("lockable-blocks")) {
            Material material = Material.getMaterial(lockableBlock.toUpperCase());
            if (material == null) continue;

            if (block.getType() == material) {
                return true;
            }
        }
        return false;
    }

    /**
     * Show the owned, trusted, and locked blocks around a player using particles
     *
     * @param   player  The player to show the particles to
     */
    public void showLockedLocations(Player player) {
        List<Location> owned = new ArrayList<>();
        List<Location> trusted = new ArrayList<>();
        List<Location> locked = new ArrayList<>();
        for (String key : Main.locked.getKeys(false)) {
            String[] split = key.split("=");
            Location loc = new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]));
            if (loc.getWorld() == player.getLocation().getWorld() && loc.distance(player.getLocation()) <= 15) {
                String owner = new LockManager(loc.getBlock()).getLocker();
                if (owner != null) {
                    if (owner.equals(player.getUniqueId().toString())) {
                        owned.add(loc);
                    } else if (new TrustManager(Bukkit.getOfflinePlayer(UUID.fromString(owner)), player).isTrusted()) {
                        trusted.add(loc);
                    } else {
                        locked.add(loc);
                    }
                }
            }
        }

        List<List<Location>> locations = Arrays.asList(owned, trusted, locked);
        particleCube(player, locations.get(0), Color.LIME, 0.25);
        particleCube(player, locations.get(1), Color.YELLOW, 0.25);
        particleCube(player, locations.get(2), Color.RED, 0.25);
    }

    public void particleCube(Player player, List<Location> locations, Color color, double distance) {
        List<Location> result = new ArrayList<>();
        for (Location loc : locations) {
            Location corner1 = loc.clone();
            Location corner2 = loc.clone().add(1, 1, 1);

            double minX = Math.min(corner1.getX(), corner2.getX());
            double minY = Math.min(corner1.getY(), corner2.getY());
            double minZ = Math.min(corner1.getZ(), corner2.getZ());
            double maxX = Math.max(corner1.getX(), corner2.getX());
            double maxY = Math.max(corner1.getY(), corner2.getY());
            double maxZ = Math.max(corner1.getZ(), corner2.getZ());

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
                            } else {
                                result.add(add);
                            }
                        }
                    }
                }
            }
        }
        Particle.DustOptions dust = new Particle.DustOptions(color, 0.7f);
        for (Location loc : result) {
            player.spawnParticle(Particle.REDSTONE, loc, 0, 0, 0, 0, dust);
        }
    }

    /**
     * Attempts to lock a block
     *
     * @param   player  Player that triggered the lock
     */
    public void attemptLock(Player player, ItemStack item) {

        // Check If Block Is Lockable
        if (!isLockable()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
            new MessageManager("locking.lock.invalid")
                    .replace("%block%", getName())
                    .send(player);
            return;
        }

        // Check If Player Placed Block
        String placer = getPlacer();
        if (!(placer == null && (Main.config.getBoolean("allow-unplaced-locking") || CommandBypass.check(player)))) {
            if (placer == null || !placer.equals(player.getUniqueId().toString())) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);

                String placerString = "N/A";
                if (placer != null) Bukkit.getOfflinePlayer(UUID.fromString(placer)).getName();
                new MessageManager("locking.lock.fail")
                        .replace("%block%", getName())
                        .replace("%player%", placerString)
                        .send(player);
                return;
            }
        } else {
            attemptPlace(player);
        }

        // Check If Block Is Already Locked
        if (isLocked()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
            new MessageManager("locking.lock.again")
                    .replace("%block%", getName())
                    .replace("%player%", Bukkit.getOfflinePlayer(UUID.fromString(getLocker())).getName())
                    .send(player);
            return;
        }

        // Check if player has enough empty slots
        if (getLockedCount(player) >= new SlotManager("locks", player).getCount()) {
            String type = "locks";
            new MessageManager("slots.limit")
                    .replace("%type%", type.substring(0, type.length() - 1))
                    .replace("%target%", getName())
                    .replace("%total%", String.valueOf(Main.slots.getInt(player.getUniqueId() + "." + type)).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                    .send(player);
            return;
        }

        // Attempt to lock double chest / door
        if (attemptLockDoubleChest(player, item) || attemptLockDoor(player, item)) return;

        if (!(block.getState() instanceof Chest chest && chest.getInventory() instanceof DoubleChestInventory) && !block.getType().toString().contains("_DOOR")) {
            lock(player, item);

            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
            new MessageManager("locking.lock.success")
                    .replace("%block%", getName())
                    .send(player);
        }
    }

    /**
     * Removes lock data
     *
     * @param   player  Player that triggered the lock
     */
    public void lock(Player player, ItemStack item) {
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
        Main.locked.set(getId() + ".locker", player.getUniqueId().toString());
        save();
    }

    /**
     * Attempts to lock a double chest
     *
     * @param   player  Player that triggered the lock
     *
     * @return          True if successful, false if not
     */
    public boolean attemptLockDoubleChest(Player player, ItemStack item) {
        // Check if block is a double chest
        if (block.getState() instanceof Chest chest && chest.getInventory() instanceof DoubleChestInventory doubleChest) {
            if (new SlotManager("locks", player).getCount() > (getLockedCount(player) + 1)) {
                for (Location location : new Location[]{doubleChest.getLeftSide().getLocation(), doubleChest.getRightSide().getLocation()}) {
                    new LockManager(location.getBlock()).lock(player, item);
                }

                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
                new MessageManager("locking.lock.success")
                        .replace("%block%", getName())
                        .send(player);
                return true;
            } else {
                String type = "locks";
                new MessageManager("slots.limit")
                        .replace("%type%", type.substring(0, type.length() - 1))
                        .replace("%target%", getName())
                        .replace("%total%", String.valueOf(Main.slots.getInt(player.getUniqueId() + "." + type)).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                        .send(player);
            }
        }
        return false;
    }

    /**
     * Attempts to lock a door
     *
     * @param   player  Player that triggered the lock
     *
     * @return          True if successful, false if not
     */
    public boolean attemptLockDoor(Player player, ItemStack item) {
        if (block.getType().toString().contains("_DOOR")) {
            if (new SlotManager("locks", player).getCount() > (getLockedCount(player) + 1)) {
                //noinspection DuplicatedCode
                Door door = (Door) block.getState().getBlockData();
                Location top = block.getLocation();
                Location bottom = block.getLocation();

                if (door.getHalf() == Bisected.Half.TOP) {
                    bottom = block.getLocation().subtract(0, 1, 0);
                }
                if (door.getHalf() == Bisected.Half.BOTTOM) {
                    top = block.getLocation().add(0, 1, 0);
                }

                for (Location location : new Location[]{top, bottom}) {
                    new LockManager(location.getBlock()).lock(player, item);
                }

                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
                new MessageManager("locking.lock.success")
                        .replace("%block%", getName())
                        .send(player);
                return true;
            } else {
                String type = "locks";
                new MessageManager("slots.limit")
                        .replace("%type%", type.substring(0, type.length() - 1))
                        .replace("%target%", getName())
                        .replace("%total%", String.valueOf(Main.slots.getInt(player.getUniqueId() + "." + type)).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                        .send(player);
            }
        }
        return false;
    }

    public void checkLockDoubleChest(Player player) {
        if (block.getState() instanceof Chest chest && chest.getInventory() instanceof DoubleChestInventory doubleChest) {
            location(player, doubleChest.getRightSide().getLocation(), doubleChest.getLeftSide().getLocation());
        }
    }

    public void checkLockDoor(Player player) {
        if (block.getType().toString().contains("_DOOR")) {
            //noinspection DuplicatedCode
            Door door = (Door) block.getState().getBlockData();
            Location top = block.getLocation();
            Location bottom = block.getLocation();

            if (door.getHalf() == Bisected.Half.TOP) {
                bottom = block.getLocation().subtract(0, 1, 0);
            }
            if (door.getHalf() == Bisected.Half.BOTTOM) {
                top = block.getLocation().add(0, 1, 0);
            }

            location(player, top, bottom);
        }
    }

    private void location(Player player, Location one, Location two) {
        Location[] locations = {one, two};
        for (Location loc : locations) {
            Block locBlock = loc.getBlock();
            if (new LockManager(locBlock).isLocked()) {
                String owner = new LockManager(locBlock).getLocker();
                String block0 = new LockManager(locations[0].getBlock()).getId();
                String block1 = new LockManager(locations[0].getBlock()).getId();

                Main.locked.set(block0 + ".locker", owner);
                Main.locked.set(block1 + ".locker", owner);
                Main.locked.set(block0 + ".placer", owner);
                Main.locked.set(block1 + ".placer", owner);
                save();

                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
                return;
            }
        }
    }

    /**
     * Attempts to unlock a block
     *
     * @param   player  The player to play sounds for
     */
    public void attemptUnlock(Player player, ItemStack item) {

        // Check If Block Is Locked
        if (!isLocked()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
            new MessageManager("locking.unlock.invalid")
                    .replace("%block%", getName())
                    .send(player);
            return;
        }

        // Check If Player Placed Block
        String placer = getPlacer();
        if ((placer == null || !placer.equals(player.getUniqueId().toString())) && !player.hasPermission("vanadium.command.bypass")) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
            new MessageManager("locking.unlock.fail")
                    .replace("%block%", getName())
                    .replace("%player%", Bukkit.getOfflinePlayer(UUID.fromString(getPlacer())).getName())
                    .send(player);
            return;
        }

        // Attempt to unlock double chest / door
        if (attemptUnlockDoubleChest(player, item) || attemptUnlockDoor(player, item)) return;

        if (!(block.getState() instanceof Chest chest && chest.getInventory() instanceof DoubleChestInventory) && !block.getType().toString().contains("_DOOR")) {
            unlock(player, item);

            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
            new MessageManager("locking.unlock.success")
                    .replace("%block%", getName())
                    .send(player);
        }
    }

    /**
     * Removes {@code locker} data
     *
     * @param   player  The player to play the unlock sound for
     */
    public void unlock(Player player, ItemStack item) {
        if (item != null && item.getItemMeta() instanceof Damageable damage) {
            int current = damage.getDamage();
            if (current - 1 <= item.getType().getMaxDurability()) {
                damage.setDamage(current - 1);
                item.setItemMeta(damage);
            } else {
                player.playSound(player.getLocation(),Sound.ENTITY_ITEM_BREAK,1,1);
                item.setAmount(0);
            }
        }
        Main.locked.set(getId() + ".locker", null);
        save();
    }

    /**
     * Attempts to unlock a double chest
     *
     * @param   player  Player to use in the {@code unlock} method
     *
     * @return          True if unlock was successful, false if not
     */
    public boolean attemptUnlockDoubleChest(Player player, ItemStack item) {
        if (block.getState() instanceof Chest chest && chest.getInventory() instanceof DoubleChestInventory doubleChest) {
            attemptUnlockDouble(player, item, doubleChest.getLeftSide().getLocation(), doubleChest.getRightSide().getLocation());
            return true;
        }
        return false;
    }

    /**
     * Attempts to unlock a door
     *
     * @param   player  Player to use in the {@code unlock} method
     *
     * @return          True if unlock was successful, false if not
     */
    public boolean attemptUnlockDoor(Player player, ItemStack item) {
        if (block.getType().toString().contains("_DOOR")) {
            //noinspection DuplicatedCode
            Door door = (Door) block.getState().getBlockData();
            Location top = block.getLocation();
            Location bottom = block.getLocation();

            if (door.getHalf() == Bisected.Half.TOP) {
                bottom = block.getLocation().subtract(0, 1, 0);
            }
            if (door.getHalf() == Bisected.Half.BOTTOM) {
                top = block.getLocation().add(0, 1, 0);
            }

            attemptUnlockDouble(player, item, top, bottom);
            return true;
        }
        return false;
    }

    private void attemptUnlockDouble(Player player, ItemStack item, Location one, Location two) {
        for (Location location : new Location[]{one, two}) {
            new LockManager(location.getBlock()).unlock(player, item);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 2);
        new MessageManager("locking.unlock.success")
                .replace("%block%", getName())
                .send(player);
    }

    /**
     * Checks if a block has place data
     *
     * @return  True if yes, false if no
     */
    public boolean isPlaced() {
        return Main.locked.contains(getId());
    }

    /**
     * @return  Whoever placed {@code block}
     */
    public String getPlacer() {
        if (Main.locked.contains(getId() + ".placer")) return Main.locked.getString(getId() + ".placer");
        return null;
    }

    /**
     * Saves place data
     *
     * @param   player  The player to set as the placer
     */
    public void place(Player player) {
        Main.locked.set(getId() + ".placer", player.getUniqueId().toString());
        save();
    }

    /**
     * Attempts to place a block
     */
    public void attemptPlace(Player player) {
        if (attemptPlaceDoor(player)) return;

        place(player);
    }

    /**
     * Attempts to place a door
     *
     * @param   player  Player that triggered the place
     *
     * @return          True if successful, false if not
     */
    public boolean attemptPlaceDoor(Player player) {
        if (block.getType().toString().contains("_DOOR")) {
            //noinspection DuplicatedCode
            Door door = (Door) block.getState().getBlockData();
            Location top = block.getLocation();
            Location bottom = block.getLocation();

            if (door.getHalf() == Bisected.Half.TOP) {
                bottom = block.getLocation().subtract(0, 1, 0);
            }
            if (door.getHalf() == Bisected.Half.BOTTOM) {
                top = block.getLocation().add(0, 1, 0);
            }

            for (Location location : new Location[]{top, bottom}) {
                new LockManager(location.getBlock()).place(player);
            }

            return true;
        }
        return false;
    }

    /**
     * Removes place data
     */
    public void unplace() {
        Main.locked.set(getId(), null);
        save();
    }

    /**
     * Attempts to place a block
     */
    public void attemptUnplace() {
        if (attemptUnplaceDoor()) return;

        unplace();
    }

    /**
     * Attempts to place a door
     *
     * @return  True if successful, false if not
     */
    public boolean attemptUnplaceDoor() {
        if (block.getType().toString().contains("_DOOR")) {
            //noinspection DuplicatedCode
            Door door = (Door) block.getState().getBlockData();
            Location top = block.getLocation();
            Location bottom = block.getLocation();

            if (door.getHalf() == Bisected.Half.TOP) {
                bottom = block.getLocation().subtract(0, 1, 0);
            }
            if (door.getHalf() == Bisected.Half.BOTTOM) {
                top = block.getLocation().add(0, 1, 0);
            }

            for (Location location : new Location[]{top, bottom}) {
                new LockManager(location.getBlock()).unplace();
            }

            return true;
        }
        return false;
    }

    /**
     * @return  The ID of the block in the data file
     */
    public String getId() {
        return block.getWorld().getName() + "=" + block.getX() + "=" + block.getY() + "=" + block.getZ() + "=" + block.getType().name();
    }

    /**
     * @return  The name of the block
     */
    public String getName() {
        return WordUtils.capitalizeFully(block.getType().name().toLowerCase().replaceAll("_", " "));
    }

    /**
     * Get the current number of blocks {@code player} has locked
     *
     * @param   player  The player
     *
     * @return          How many blocks {@code player} currently has locked
     */
    public int getLockedCount(Player player) {
        int i = 0;
        for (String key : Main.locked.getKeys(false)) if (Objects.equals(Main.locked.getString(key + ".locker"), player.getUniqueId().toString())) {
            i++;
        }
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
     * @param   player  The player to check
     *
     * @return          True if holding, false if not
     */
    public boolean holdingLockTool(Player player) {
        return ItemManager.isSame(player.getInventory().getItemInMainHand(), getLockTool());
    }

    /**
     * Checks if locked block types are still the same as the saved types
     */
    public void check() {
        for (String key : Main.locked.getKeys(false)) {
            String[] split = key.split("=");
            Location loc = new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]));
            if (!loc.getBlock().getType().name().equalsIgnoreCase(split[4])) Main.locked.set(key, null);
        }
        save();
    }

    /**
     * Saves {@code locked.yml}
     */
    private void save() {
        new ConfigManager("locked.yml", true).saveData(Main.locked);
    }
}
