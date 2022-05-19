package xyz.srnyx.vanadium.commands;

import org.apache.commons.lang.WordUtils;

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


@SuppressWarnings("NullableProblems")
public class CommandTrustList implements TabExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (PlayerManager.noPermission(sender, "vanadium.trustlist")) return true;
        final Player player = (Player) sender;

        if (args.length != 0) {
            final Block block = player.getTargetBlockExact(6);
            final LockManager lock = new LockManager(block, player);

            if (args.length == 1) {
                if (block != null && lock.isLocked()) {
                    if (Objects.equals(args[0], "master")) {
                        new TrustManager(player, Bukkit.getOfflinePlayer(player.getUniqueId())).trustList(null);
                        return true;
                    }

                    if (Objects.equals(args[0], "block")) {
                        if (lock.isLockedForPlayer()) {
                            if (player.hasPermission("vanadium.trustlist.others")) {
                                new TrustManager(player, Bukkit.getOfflinePlayer(lock.getLocker())).trustList(block);
                                return true;
                            }

                            String playerString = "N/A";
                            if (lock.getLocker() != null) playerString = Bukkit.getOfflinePlayer(lock.getLocker()).getName();

                            new MessageManager("locking.block-locked")
                                    .replace("%block%", WordUtils.capitalizeFully(block.getType().name().replace("_", " ")))
                                    .replace("%player%", playerString)
                                    .send(player);
                            return true;
                        }

                        new TrustManager(player, Bukkit.getOfflinePlayer(player.getUniqueId())).trustList(block);
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

        new TrustManager(player, Bukkit.getOfflinePlayer(player.getUniqueId())).trustList(null);
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> suggestions = new ArrayList<>();
        final List<String> results = new ArrayList<>();
        final Player player = (Player) sender;
        final Block block = player.getTargetBlockExact(6);

        if (args.length == 1) {
            if (block != null && new LockManager(block, null).isLocked()) {
                if (player.hasPermission("vanadium.trustlist.others")) suggestions.add("master");
                suggestions.add("block");
            } else if (player.hasPermission("vanadium.trustlist.others")) {
                if (args[args.length - 1].length() == 0) {
                    for (final Player online : Bukkit.getOnlinePlayers()) suggestions.add(online.getName());
                } else {
                    for (final OfflinePlayer offline : Bukkit.getOfflinePlayers()) suggestions.add(offline.getName());
                }
            }
        } else if (args.length == 2 && Objects.equals(args[0], "master") && player.hasPermission("vanadium.trustlist.others")) {
            if (args[args.length - 1].length() == 0) {
                for (final Player online : Bukkit.getOnlinePlayers()) suggestions.add(online.getName());
            } else {
                for (final OfflinePlayer offline : Bukkit.getOfflinePlayers()) suggestions.add(offline.getName());
            }
        }

        for (final String suggestion : suggestions) if (suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) results.add(suggestion);
        return results;
    }
}
