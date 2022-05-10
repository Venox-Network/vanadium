package xyz.srnyx.vanadium.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import xyz.srnyx.vanadium.managers.DataManager;
import xyz.srnyx.vanadium.managers.MessageManager;
import xyz.srnyx.vanadium.managers.PlayerManager;
import xyz.srnyx.vanadium.managers.TrustManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class CommandTrust implements TabExecutor {
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (PlayerManager.isNotPlayer(sender)) return true;
        if (PlayerManager.noPermission(sender, "vanadium.trust")) return true;
        Player player = (Player) sender;

        if (args.length == 1) {
            OfflinePlayer target = PlayerManager.getOfflinePlayer(args[0]);
            if (target == null) {
                new MessageManager("errors.invalid-player")
                        .replace("%player%", args[0])
                        .send(player);
                return true;
            }
            new TrustManager(player, target).trust();
            return true;
        }

        new MessageManager("errors.invalid-arguments").send(player);
        return true;
    }

    @SuppressWarnings("NullableProblems")
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        List<String> results = new ArrayList<>();

        if (!(sender instanceof Player player)) return results;

        if (args.length == 1) {
            List<UUID> trusted = DataManager.trusted.get(player.getUniqueId());
            if (args[0].length() == 0) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (trusted == null || !trusted.contains(online.getUniqueId()) && online != player) suggestions.add(online.getName());
                }
            } else {
                for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
                    if (trusted == null || !trusted.contains(offline.getUniqueId()) && offline.getUniqueId() != player.getUniqueId()) suggestions.add(offline.getName());
                }
            }
        }

        for (String suggestion : suggestions) if (suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) results.add(suggestion);
        return results;
    }
}
