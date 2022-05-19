package xyz.srnyx.vanadium.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.vanadium.managers.DataManager;
import xyz.srnyx.vanadium.managers.PlayerManager;


public class CommandSave implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!PlayerManager.noPermission(sender, "vanadium.save")) new DataManager().save();
        return true;
    }
}
