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
import java.util.Locale;
import java.util.UUID;


public class CommandTrust implements TabExecutor {
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (PlayerManager.isNotPlayer(sender)) return true;
        if (PlayerManager.noPermission(sender, "vanadium.trust")) return true;
        final Player player = (Player) sender;

        if (args.length == 1) {
            final OfflinePlayer target = PlayerManager.getOfflinePlayer(args[0]);

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
        final List<String> suggestions = new ArrayList<>();
        final List<String> results = new ArrayList<>();
        final Player player = (Player) sender;

        if (args.length == 1) {
            final List<UUID> trusted = DataManager.trusted.get(player.getUniqueId());
            if (args[0].length() == 0) {
                for (final Player online : Bukkit.getOnlinePlayers()) {
                    if (trusted == null || !trusted.contains(online.getUniqueId()) && online != player) suggestions.add(online.getName());
                }
            } else {
                for (final OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
                    if (trusted == null || !trusted.contains(offline.getUniqueId()) && offline.getUniqueId() != player.getUniqueId()) suggestions.add(offline.getName());
                }
            }
        }

        for (final String suggestion : suggestions) if (suggestion.toLowerCase(Locale.ENGLISH).startsWith(args[args.length - 1].toLowerCase(Locale.ENGLISH))) results.add(suggestion);
        return results;
    }
}
