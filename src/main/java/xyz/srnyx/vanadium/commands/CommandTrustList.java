package xyz.srnyx.vanadium.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.vanadium.managers.LockManager;
import xyz.srnyx.vanadium.managers.MessageManager;
import xyz.srnyx.vanadium.managers.PlayerManager;
import xyz.srnyx.vanadium.managers.TrustManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class CommandTrustList implements TabExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (PlayerManager.noPermission(sender, "vanadium.trustlist")) return true;
        final Player player = (Player) sender;
        final OfflinePlayer op = Bukkit.getOfflinePlayer(player.getUniqueId());

        if (args.length != 0) {
            final Block block = player.getTargetBlockExact(6);
            final LockManager lock = new LockManager(block, player);

            if (args.length == 1) {
                if (block != null && lock.isLocked()) {
                    if (Objects.equals(args[0], "master")) {
                        new TrustManager(player, op).trustList(null);
                        return true;
                    }

                    if (Objects.equals(args[0], "block")) {
                        if (lock.isLockedForPlayer()) {
                            if (player.hasPermission("vanadium.trustlist.others")) {
                                new TrustManager(player, Bukkit.getOfflinePlayer(lock.getLocker())).trustList(block);
                                return true;
                            }

                            new TrustManager(player, null).locked(block);
                            return true;
                        }

                        new TrustManager(player, op).trustList(block);
                        return true;
                    }
                }

                if (player.hasPermission("vanadium.trustlist.others")) {
                    final OfflinePlayer target = PlayerManager.getOfflinePlayer(args[0]);
                    if (target != null) {
                        new TrustManager(player, target).trustList(null);
                        return true;
                    }

                    new MessageManager("errors.invalid-player")
                            .replace("%player%", args[0])
                            .send(player);
                    return true;
                }
            }

            if (args.length == 2 && block != null && lock.isLocked() && player.hasPermission("vanadium.trustlist.others")) {
                final OfflinePlayer target = PlayerManager.getOfflinePlayer(args[1]);
                if (target != null) {
                    if (Objects.equals(args[0], "master")) {
                        new TrustManager(player, target).trustList(null);
                        return true;
                    }

                    new MessageManager("errors.invalid-arguments").send(player);
                    return true;
                }

                new MessageManager("errors.invalid-player")
                        .replace("%player%", args[0])
                        .send(player);
                return true;
            }

            new MessageManager("errors.invalid-arguments").send(player);
            return true;
        }

        new TrustManager(player, op).trustList(null);
        return true;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        final List<String> suggestions = new ArrayList<>();
        final List<String> results = new ArrayList<>();
        final Player player = (Player) sender;
        final Block block = player.getTargetBlockExact(6);

        if (args.length == 1) {
            if (block != null && new LockManager(block, null).isLocked()) {
                if (player.hasPermission("vanadium.trustlist.others")) suggestions.add("master");
                suggestions.add("block");

            } else if (player.hasPermission("vanadium.trustlist.others")) {
                if (args[0].length() == 0) {
                    for (final Player online : Bukkit.getOnlinePlayers()) suggestions.add(online.getName());
                } else for (final OfflinePlayer offline : Bukkit.getOfflinePlayers()) suggestions.add(offline.getName());
            }

        } else if (args.length == 2 && Objects.equals(args[0], "master") && player.hasPermission("vanadium.trustlist.others")) {
            if (args[1].length() == 0) {
                for (final Player online : Bukkit.getOnlinePlayers()) suggestions.add(online.getName());
            } else for (final OfflinePlayer offline : Bukkit.getOfflinePlayers()) suggestions.add(offline.getName());
        }

        for (final String suggestion : suggestions) if (suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) results.add(suggestion);
        return results;
    }
}
