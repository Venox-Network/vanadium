package xyz.srnyx.vanadium.managers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.entity.Player;


public class PlayerManager {
    @SuppressWarnings({"CanBeFinal"})
    private Player player;
    private String sPlayer;
    private CommandSender sender;

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
     * Constructor for {@code PlayerManager}
     *
     * @param   sender  The sender to use for the methods
     */
    public PlayerManager(CommandSender sender) {
        this.sender = sender;
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

    /**
     * Check if a {@code CommandSender} is a player
     *
     * @return  True if player, false if non-player
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isPlayer() {
        if (sender instanceof Player) return true;
        new MessageManager("errors.console-forbidden").send(sender);
        return false;
    }

    /**
     * Check if a {@code CommandSender} is a player and has a certain permission
     *
     * @param   permission  The permission to check them for
     *
     * @return              True if player & has permission, false if non-player or doesn't have permission
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasPermission(String permission) {
        if (sender instanceof Player player) {
            if (player.hasPermission(permission)) {
                return true;
            } else {
                new MessageManager("errors.no-permission")
                        .replace("%permission%", permission)
                        .send(player);
                return false;
            }
        } else {
            return true;
        }
    }
}
