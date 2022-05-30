package network.venox.vanadium.managers;

import com.earth2me.essentials.Essentials;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.entity.Player;


public class PlayerManager {
    /**
     * @param   player  The player to get the {@code OfflinePlayer} of
     *
     * @return          The {@code OfflinePlayer}
     */
    public static OfflinePlayer getOfflinePlayer(String player) {
        for (final OfflinePlayer op : Bukkit.getOfflinePlayers()) if (op.getName() != null && op.getName().equalsIgnoreCase(player)) return op;
        return null;
    }

    /**
     * Check if a {@code CommandSender} is a player
     *
     * @param   sender  The {@code CommandSender} to check
     *
     * @return          True if player, false if non-player
     */
    public static boolean isNotPlayer(CommandSender sender) {
        if (sender instanceof Player) return false;
        new MessageManager("errors.console-forbidden").send(sender);
        return true;
    }

    /**
     * Check if a player is vanished
     *
     * @param   player  The player to check
     *
     * @return          True if vanished, false if not
     */
    public static boolean isVanished(Player player) {
        for (final MetadataValue meta : player.getMetadata("vanished")) if (meta.asBoolean()) return true;
        return false;
    }

    /**
     * Check if a {@code CommandSender} is a player and has a certain permission
     *
     * @param   sender      The {@code CommandSender} to check
     * @param   permission  The permission to check them for
     *
     * @return              True if player & has permission, false if non-player or doesn't have permission
     */
    public static boolean noPermission(CommandSender sender, String permission) {
        if (sender instanceof Player player) {
            if (player.hasPermission(permission)) {
                return false;
            } else {
                new MessageManager("errors.no-permission")
                        .replace("%permission%", permission)
                        .send(player);
                return true;
            }
        } else return false;
    }

    /**
     * Check if a player has a certain scoreboard tag
     *
     * @param   player  The player to check
     * @param   check   The tag to check for
     *
     * @return          True if yes, false if no
     */
    public static boolean hasScoreboardTag(Player player, String check) {
        for (final String tag : player.getScoreboardTags()) if (tag.equals(check)) return true;
        return false;
    }

    /**
     * Check if a player is AFK
     *
     * @param   player  The player to check
     *
     * @return          True if AFK, false if not
     */
    public static boolean isAFK(Player player) {
        final Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        return essentials != null && essentials.getUser(player).isAfk();
    }
}
