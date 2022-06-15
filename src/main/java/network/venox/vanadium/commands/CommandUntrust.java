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


public class CommandUntrust implements TabExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (PlayerManager.isNotPlayer(sender)) return true;
        if (PlayerManager.noPermission(sender, "vanadium.untrust")) return true;

        final Player player = (Player) sender;
        final Block block = player.getTargetBlockExact(6);

        if (args.length == 1 && (block == null || !new LockManager(block, player).isLockable())) {
            final OfflinePlayer target = PlayerManager.getOfflinePlayer(args[0]);
            new TrustManager(player, target).untrustPlayer();
            return true;
        }

        if (args.length == 2) {
            final OfflinePlayer target = PlayerManager.getOfflinePlayer(args[1]);
            final TrustManager trust = new TrustManager(player, target);

            if (target == null) {
                new MessageManager("errors.invalid-player")
                        .replace("%player%", args[0])
                        .send(player);
                return true;
            }

            if (Objects.equals(args[0], "master")) {
                trust.untrustPlayer();
                return true;
            }

            if (!Objects.equals(args[0], "block") || block == null) return true;

            // Cancel if block is locked for player
            if (new LockManager(block, player).isLockedForPlayer()) {
                trust.locked(block);
                return true;
            }

            // Attempt untrust Door
            if (block.getType().toString().contains("_DOOR")) {
                for (final Location door : Main.door(block)) trust.untrustBlock(door.getBlock(), true);
                trust.success("untrust", block);
                return true;
            }

            // Attempt untrust Double Chest
            if (block.getState() instanceof Chest chest && chest.getInventory() instanceof DoubleChestInventory doubleChest) {
                for (final Location loc : new Location[]{doubleChest.getLeftSide().getLocation(), doubleChest.getRightSide().getLocation()}) {
                    trust.untrustBlock(loc.getBlock(), true);
                }
                trust.success("untrust", block);
                return true;
            }

            // Untrust block
            trust.untrustBlock(block, false);
            return true;
        }

        new MessageManager("errors.invalid-arguments").send(player);
        return true;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        final List<String> suggestions = new ArrayList<>();
        final List<String> results = new ArrayList<>();
        final Player player = (Player) sender;
        final Block block = player.getTargetBlockExact(6);

        //TODO Improve all the code below. Hard because of the suggestions and results lists.
        if (args.length == 1) {
            if (block != null && new LockManager(block, null).isLocked()) {
                suggestions.add("master");
                suggestions.add("block");
            } else for (final UUID id : DataManager.trusted.get(player.getUniqueId())) suggestions.add(Bukkit.getOfflinePlayer(id).getName());
        } else if (args.length == 2) {
            if (Objects.equals(args[0], "master")) for (final UUID id : DataManager.trusted.get(player.getUniqueId())) suggestions.add(Bukkit.getOfflinePlayer(id).getName());
            if (Objects.equals(args[0], "block")) for (final UUID id : DataManager.lockedTrusted.get(player.getTargetBlockExact(6))) suggestions.add(Bukkit.getOfflinePlayer(id).getName());
        }

        for (final String suggestion : suggestions) if (suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) results.add(suggestion);
        return results;
    }
}
