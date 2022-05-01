package xyz.srnyx.vanadium.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.managers.MessageManager;
import xyz.srnyx.vanadium.managers.PlayerManager;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("NullableProblems")
public class CommandSlot implements TabExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!new PlayerManager(sender).hasPermission("vanadium.slot")) return true;

        //<player> <type> [action] [amount]
        Player player = Bukkit.getServer().getPlayer(args[0]);
        String type = args[1]; // "locks" or "trusts"
        if (player != null) {
            int original = Main.slots.getInt(player.getUniqueId() + "." + type);

            //<type> <player>
            if (args.length == 2) {
                // %slot%
                String slot;
                if (original != 1) {
                    slot = "slots";
                } else slot = "slot";

                new MessageManager("slots.command.get")
                        .replace("%target%", player.getName())
                        .replace("%total%", String.valueOf(original).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                        .replace("%type%", type.substring(0, type.length() - 1))
                        .replace("%slot%", slot)
                        .send(sender);
            }

            //<type> <player> <action> <amount>
            if (args.length == 4) {
                String action = args[2];
                double amount = Double.parseDouble(args[3]);
                double calc = 0;

                if (action.equalsIgnoreCase("add")) calc = original + amount;
                if (action.equalsIgnoreCase("remove")) calc = original - amount;
                if (action.equalsIgnoreCase("set")) calc = amount;

                Main.slots.set(player.getUniqueId() + "." + type, calc);

                // %slot%
                String slot;
                if (amount != 1) {
                    slot = "slots";
                } else slot = "slot";

                new MessageManager("slots.command." + action)
                        .replace("%target%", player.getName())
                        .replace("%count%", String.valueOf(amount).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                        .replace("%type%", type.substring(0, type.length() - 1))
                        .replace("%slot%", slot)
                        .replace("%total%", String.valueOf(calc).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                        .send(sender);
            }
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();
        List<String> results = new ArrayList<>();

        if (args.length == 1) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                suggestions.add(online.getName());
            }
        }
        if (args.length == 2) {
            suggestions.add("locks");
            suggestions.add("trusts");
        }
        if (args.length == 3) {
            suggestions.add("add");
            suggestions.add("remove");
            suggestions.add("set");
        }

        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                results.add(suggestion);
            }
        }
        return results;
    }
}
