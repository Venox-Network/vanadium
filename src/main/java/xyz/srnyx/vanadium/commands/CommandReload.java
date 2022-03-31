package xyz.srnyx.vanadium.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.managers.MessageManager;


public class CommandReload implements CommandExecutor {
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!Main.hasPermission(sender, "vanadium.reload")) return true;

        Main.loadFiles();
        new MessageManager("plugin.reload").send(sender);
        return true;
    }
}