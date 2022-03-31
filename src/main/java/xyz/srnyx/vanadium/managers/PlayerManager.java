package xyz.srnyx.vanadium.managers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.entity.Player;


public class PlayerManager {
    @SuppressWarnings({"CanBeFinal"})
    private Player player;
    private String sPlayer;

    /**
     * Constructor for {@code PlayerManager}
     *
     * @param   player  The player to use for the methods
     */
    public PlayerManager(Player player) {
        this.player = player;
    }

    /**
     * Constructor for {@code PlayerManager}
     *
     * @param   player  The player to use for the methods
     */
    public PlayerManager(String player) {
        this.sPlayer = player;
    }

    /**
     * Check if a player is vanished
     *
     * @return  True if vanished, false if not
     */
    public boolean isVanished() {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }

    /**
     * Get the {@code OfflinePlayer} of a {@code Player}
     *
     * @return  The {@code OfflinePlayer}
     */
    public OfflinePlayer getOfflinePlayer() {
        for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
            if (op.getName() != null && op.getName().equalsIgnoreCase(sPlayer)) {
                return op;
            }
        }
        return null;
    }

    /**
     * Check if a player has a certain scoreboard tag
     *
     * @param   check   The tag to check for
     *
     * @return          True if yes, false if no
     */
    public boolean hasScoreboardTag(String check) {
        for (String tag : player.getScoreboardTags()) {
            if (tag.equals(check)) {
                return true;
            }
        }
        return false;
    }
}
