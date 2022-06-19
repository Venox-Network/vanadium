package network.venox.vanadium.commands;

import network.venox.vanadium.Main;
import network.venox.vanadium.managers.DataManager;
import network.venox.vanadium.managers.LockManager;
import network.venox.vanadium.managers.MessageManager;
import network.venox.vanadium.managers.PlayerManager;
import network.venox.vanadium.managers.TrustManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class CommandTrust implements TabExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (PlayerManager.isNotPlayer(sender)) return true;
        if (PlayerManager.noPermission(sender, "vanadium.trust")) return true;
        final Player player = (Player) sender;
        final Block block = player.getTargetBlockExact(6);
        final LockManager lock = new LockManager(block, player);

        if (args.length == 1 && (block == null || !lock.isLockable())) {
            final OfflinePlayer target = PlayerManager.getOfflinePlayer(args[0]);
            new TrustManager(player, target).trustPlayer();
            return true;
        }

        if (args.length == 2) {
            final OfflinePlayer target = PlayerManager.getOfflinePlayer(args[1]);
            final TrustManager trust = new TrustManager(player, target);

            if (target == null) {
                new MessageManager("errors.invalid-player", cmd, args)
                        .replace("%player%", args[1])
                        .send(player);
                return true;
            }

            if (Objects.equals(args[0], "master")) {
                trust.trustPlayer();
                return true;
            }

            if (!Objects.equals(args[0], "block") || block == null) return true;

            // Cancel if block is locked for player
            if (lock.isLockedForPlayer()) {
                trust.locked(block);
                return true;
            }

            // Cancel if player is trying to trust themselves
            if (target.getUniqueId() == player.getUniqueId()) {
                new MessageManager("trusting.self", cmd, args).send(player);
                return true;
            }

            // Attempt trust door
            if (block.getType().toString().contains("_DOOR")) {
                for (final Location door : Main.door(block)) trust.trustBlock(door.getBlock(), true);
                trust.success("trust", block);
                return true;
            }

            // Attempt trust Double Chest
            if (block.getState() instanceof Chest chest && chest.getInventory() instanceof DoubleChestInventory doubleChest) {
                for (final Location loc : new Location[]{doubleChest.getLeftSide().getLocation(), doubleChest.getRightSide().getLocation()}) {
                    trust.trustBlock(loc.getBlock(), true);
                }
                trust.success("trust", block);
                return true;
            }

            // Trust block
            trust.trustBlock(block, false);
            return true;
        }

        new MessageManager("errors.invalid-arguments", cmd, args).send(player);
        return true;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        final List<String> suggestions = new ArrayList<>();
        final List<String> results = new ArrayList<>();
        final Player player = (Player) sender;
        final Block block = player.getTargetBlockExact(6);

        //TODO Improve all the code below. Hard because of the suggestions and results lists.
        if (block != null && new LockManager(block, null).isLocked()) {
            if (args.length == 1) {
                suggestions.add("master");
                suggestions.add("block");
            } else if (args.length == 2) {
                final List<UUID> trusted = DataManager.lockedTrusted.get(block);
                if (args[1].length() == 0) for (final Player online : Bukkit.getOnlinePlayers()) {
                    if (trusted == null || !trusted.contains(online.getUniqueId()) && online != player) suggestions.add(online.getName());
                } else for (final OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
                    if (trusted == null || !trusted.contains(offline.getUniqueId()) && offline.getUniqueId() != player.getUniqueId()) suggestions.add(offline.getName());
                }
            }

        } else if (args.length == 1) {
            final List<UUID> trusted = DataManager.trusted.get(player.getUniqueId());
            if (args[0].length() == 0) for (final Player online : Bukkit.getOnlinePlayers()) {
                if (trusted == null || !trusted.contains(online.getUniqueId()) && online != player) suggestions.add(online.getName());
            } else for (final OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
                if (trusted == null || !trusted.contains(offline.getUniqueId()) && offline.getUniqueId() != player.getUniqueId()) suggestions.add(offline.getName());
            }
        }

        for (final String suggestion : suggestions) if (suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) results.add(suggestion);
        return results;
    }
}
