package network.venox.vanadium.commands;

import network.venox.vanadium.managers.LockManager;
import network.venox.vanadium.managers.MessageManager;
import network.venox.vanadium.managers.PlayerManager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;


public class CommandLockTool implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (PlayerManager.noPermission(sender, "vanadium.locktool")) return true;
        final Player player = (Player) sender;

        // If a player is specified, give lock tool to them instead of sender
        if (args.length == 1 && player.hasPermission("vanadium.locktool.others")) {
            final Player target = Bukkit.getPlayer(args[0]);
            if (target == null) return true;

            target.getInventory().addItem(LockManager.getLockTool());
            new MessageManager("locking.lock-tool.get", cmd, args)
                    .replace("%player%", player.getName())
                    .send(target);
            new MessageManager("locking.lock-tool.give", cmd, args)
                    .replace("%target%", args[0])
                    .send(sender);
            return true;
        }

        // Give lock tool to sender
        player.getInventory().addItem(LockManager.getLockTool());
        new MessageManager("locking.lock-tool.get", cmd, args)
                .replace("%player%", player.getName())
                .send(sender);
        return true;
    }
}
