package xyz.srnyx.vanadium.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.srnyx.vanadium.managers.LockManager;
import xyz.srnyx.vanadium.managers.MessageManager;
import xyz.srnyx.vanadium.managers.PlayerManager;


public class CommandLockTool implements CommandExecutor {
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (PlayerManager.noPermission(sender, "vanadium.locktool")) return true;
        final Player player = (Player) sender;

        // If a player is specified, give lock tool to them instead of sender
        if (args.length == 1 && player.hasPermission("vanadium.locktool.others")) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                target.getInventory().addItem(LockManager.getLockTool());
                new MessageManager("locking.lock-tool.get")
                        .replace("%player%", player.getName())
                        .send(target);
                new MessageManager("locking.lock-tool.give")
                        .replace("%target%", args[0])
                        .send(sender);
                return true;
            }
        }

        // Give lock tool to sender
        player.getInventory().addItem(LockManager.getLockTool());
        new MessageManager("locking.lock-tool.get")
                .replace("%player%", player.getName())
                .send(sender);
        return true;
    }
}
