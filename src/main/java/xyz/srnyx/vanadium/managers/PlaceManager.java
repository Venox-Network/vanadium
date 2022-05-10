package xyz.srnyx.vanadium.managers;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;

import java.util.UUID;


public record PlaceManager(Block block) {
    /**
     * Checks if a block has place data
     *
     * @return True if yes, false if no
     */
    public boolean isPlaced() {
        return DataManager.locked.containsKey(block);
    }

    /**
     * @return Whoever placed {@code block}
     */
    public UUID getPlacer() {
        UUID[] data = DataManager.locked.get(block);
        return data != null ? data[0] : null;
    }

    /**
     * Saves place data
     *
     * @param   player    The player to set as the placer
     */
    public void place(Player player) {
        DataManager.locked.put(block, new UUID[]{player.getUniqueId(), null});
        DataManager.lockedType.put(block, block.getType());
    }

    /**
     * Attempts to place a block
     *
     * @param   player    The player to set as the placer
     */
    public void attemptPlace(Player player) {
        if (attemptPlaceDoor(player)) return;
        place(player);
    }

    /**
     * Attempts to place a door
     *
     * @param player Player that triggered the place
     *
     * @return True if successful, false if not
     */
    public boolean attemptPlaceDoor(Player player) {
        if (block.getType().toString().contains("_DOOR")) {
            //noinspection DuplicatedCode
            Door door = (Door) block.getState().getBlockData();
            Location top = block.getLocation();
            Location bottom = block.getLocation();

            if (door.getHalf() == Bisected.Half.TOP) bottom = block.getLocation().subtract(0, 1, 0);
            if (door.getHalf() == Bisected.Half.BOTTOM) top = block.getLocation().add(0, 1, 0);

            for (Location location : new Location[]{top, bottom}) new PlaceManager(location.getBlock()).place(player);

            return true;
        }
        return false;
    }

    /**
     * Removes place data
     */
    public void unplace() {
        DataManager.locked.remove(block);
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
     * @return True if successful, false if not
     */
    public boolean attemptUnplaceDoor() {
        if (block.getType().toString().contains("_DOOR")) {
            //noinspection DuplicatedCode
            Door door = (Door) block.getState().getBlockData();
            Location top = block.getLocation();
            Location bottom = block.getLocation();

            if (door.getHalf() == Bisected.Half.TOP) bottom = block.getLocation().subtract(0, 1, 0);
            if (door.getHalf() == Bisected.Half.BOTTOM) top = block.getLocation().add(0, 1, 0);

            for (Location location : new Location[]{top, bottom}) new PlaceManager(location.getBlock()).unplace();

            return true;
        }
        return false;
    }
}
