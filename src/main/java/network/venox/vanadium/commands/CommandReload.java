package network.venox.vanadium.commands;

import network.venox.vanadium.Main;
import network.venox.vanadium.managers.DataManager;
import network.venox.vanadium.managers.MessageManager;
import network.venox.vanadium.managers.PlayerManager;
import network.venox.vanadium.managers.slots.LockSlotManager;
import network.venox.vanadium.managers.slots.TrustSlotManager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;


public class CommandReload implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (PlayerManager.noPermission(sender, "vanadium.reload")) return true;

        new DataManager().save();
        Main.loadFiles();

        for (final Player online: Bukkit.getOnlinePlayers()) {
            new LockSlotManager(online).stop();
            new LockSlotManager(online).start();

            new TrustSlotManager(online).stop();
            new TrustSlotManager(online).start();
        }

        new MessageManager("plugin.reload").send(sender);
        return true;
    }
}
