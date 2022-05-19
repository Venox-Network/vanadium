package xyz.srnyx.vanadium.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import xyz.srnyx.vanadium.managers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@SuppressWarnings("NullableProblems")
public class CommandTrust implements TabExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (PlayerManager.isNotPlayer(sender)) return true;
        if (PlayerManager.noPermission(sender, "vanadium.trust")) return true;
        final Player player = (Player) sender;
        final Block block = player.getTargetBlockExact(6);
        final LockManager lock = new LockManager(block, player);

        if (args.length >= 1) {
            if (args.length == 1 && (block == null || !lock.isLockable())) {
                final OfflinePlayer target = PlayerManager.getOfflinePlayer(args[0]);
                new TrustManager(player, target).trust();
                return true;
            }

            if (args.length == 2) {
                final OfflinePlayer target = PlayerManager.getOfflinePlayer(args[1]);
                if (target == null) {
                    new MessageManager("errors.invalid-player")
                            .replace("%player%", args[1])
                            .send(player);
                    return true;
                }

                if (Objects.equals(args[0], "master")) {
                    new TrustManager(player, target).trust();
                    return true;
                }

                if (Objects.equals(args[0], "block") && block != null && !lock.isLockedForPlayer()) {
                    new TrustManager(player, target).trustBlock(block);
                    return true;
                }
            }
        }

        new MessageManager("errors.invalid-arguments").send(player);
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> suggestions = new ArrayList<>();
        final List<String> results = new ArrayList<>();
        final Player player = (Player) sender;
        final List<UUID> trusted = DataManager.trusted.get(player.getUniqueId());
        final Block block = player.getTargetBlockExact(6);

        if (block != null && new LockManager(block, null).isLocked()) {
            if (args.length == 1) {
                suggestions.add("master");
                suggestions.add("block");
            } else if (args.length == 2) {
                if (args[args.length - 1].length() == 0) {
                    for (final Player online : Bukkit.getOnlinePlayers()) {
                        if (trusted == null || !trusted.contains(online.getUniqueId()) && online != player) suggestions.add(online.getName());
                    }
                } else {
                    for (final OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
                        if (trusted == null || !trusted.contains(offline.getUniqueId()) && offline.getUniqueId() != player.getUniqueId()) suggestions.add(offline.getName());
                    }
                }
            }
        } else if (args.length == 1) {
            if (args[args.length - 1].length() == 0) {
                for (final Player online : Bukkit.getOnlinePlayers()) {
                    if (trusted == null || !trusted.contains(online.getUniqueId()) && online != player) suggestions.add(online.getName());
                }
            } else {
                for (final OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
                    if (trusted == null || !trusted.contains(offline.getUniqueId()) && offline.getUniqueId() != player.getUniqueId()) suggestions.add(offline.getName());
                }
            }
        }

        for (final String suggestion : suggestions) if (suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) results.add(suggestion);
        return results;
    }
}
