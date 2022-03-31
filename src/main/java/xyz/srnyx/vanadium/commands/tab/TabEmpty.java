package xyz.srnyx.vanadium.commands.tab;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;


/**
 * Returns an empty table complete
 */
public class TabEmpty implements TabCompleter {
    @SuppressWarnings("NullableProblems")
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return new ArrayList<>();
    }
}
