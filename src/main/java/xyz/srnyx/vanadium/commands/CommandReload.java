package xyz.srnyx.vanadium.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.managers.MessageManager;
import xyz.srnyx.vanadium.managers.PlayerManager;
import xyz.srnyx.vanadium.managers.SlotManager;


public class CommandReload implements CommandExecutor {
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!new PlayerManager(sender).hasPermission("vanadium.reload")) return true;

        Main.loadFiles();
        for (Player online: Bukkit.getOnlinePlayers()) {
            new SlotManager("locks", online).stop();
            new SlotManager("locks", online).start();

            new SlotManager("trusts", online).stop();
            new SlotManager("trusts", online).start();
        }

        new MessageManager("plugin.reload").send(sender);
        return true;
    }
}