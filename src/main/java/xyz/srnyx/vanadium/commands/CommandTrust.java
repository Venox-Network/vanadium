package xyz.srnyx.vanadium.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.managers.MessageManager;
import xyz.srnyx.vanadium.managers.PlayerManager;
import xyz.srnyx.vanadium.managers.TrustManager;

import java.util.ArrayList;
import java.util.List;


public class CommandTrust implements TabExecutor {
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!new PlayerManager(sender).isPlayer()) return true;
        if (!new PlayerManager(sender).hasPermission("vanadium.trust")) return true;
        Player player = (Player) sender;

        if (args.length == 1) {
            OfflinePlayer target = new PlayerManager(args[0]).getOfflinePlayer();
            if (target == null) {
                new MessageManager("errors.invalid-player")
                        .replace("%player%", args[0])
                        .send(player);
                return true;
            }
            new TrustManager(target, player).trust();
            return true;
        }

        new MessageManager("errors.invalid-arguments").send(player);
        return true;
    }

    @SuppressWarnings("NullableProblems")
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        List<String> results = new ArrayList<>();

        if (!(sender instanceof Player player)) {
            return results;
        }

        if (args.length == 1) {
            List<String> trusted = Main.trusted.getStringList(player.getUniqueId().toString());
            if (args[0].length() == 0) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (!trusted.contains(online.getUniqueId().toString()) && online != player) {
                        suggestions.add(online.getName());
                    }
                }
            } else {
                for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
                    if (!trusted.contains(offline.getUniqueId().toString()) && offline.getUniqueId() != player.getUniqueId()) {
                        suggestions.add(offline.getName());
                    }
                }
            }
        }

        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                results.add(suggestion);
            }
        }
        return results;
    }
}
