package network.venox.vanadium.managers;

import network.venox.vanadium.Main;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.UUID;


public record PlaceManager(Block block) {
    /**
     * Checks if a block has place data
     *
     * @return  True if yes, false if no
     */
    public boolean isPlaced() {
        return DataManager.locked.containsKey(block);
    }

    /**
     * @return  Whoever placed {@code block}
     */
    public UUID getPlacer() {
        final UUID[] data = DataManager.locked.get(block);
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
        if (!attemptPlaceDoor(player)) place(player);
    }

    /**
     * Attempts to place a door
     *
     * @param   player  Player that triggered the place
     *
     * @return  True if successful, false if not
     */
    public boolean attemptPlaceDoor(Player player) {
        if (!block.getType().toString().contains("_DOOR")) return false;
        for (final Location location : Main.door(block)) new PlaceManager(location.getBlock()).place(player);
        return true;
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
        if (!attemptUnplaceDoor()) unplace();
    }

    /**
     * Attempts to place a door
     *
     * @return  True if successful, false if not
     */
    public boolean attemptUnplaceDoor() {
        if (!block.getType().toString().contains("_DOOR")) return false;
        for (final Location location : Main.door(block)) new PlaceManager(location.getBlock()).unplace();
        return true;
    }
}
