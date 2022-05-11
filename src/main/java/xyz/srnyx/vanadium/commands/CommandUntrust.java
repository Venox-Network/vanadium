package xyz.srnyx.vanadium.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import xyz.srnyx.vanadium.managers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class CommandUntrust implements TabExecutor {
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (PlayerManager.isNotPlayer(sender)) return true;
        if (PlayerManager.noPermission(sender, "vanadium.untrust")) return true;
        Player player = (Player) sender;

        if (args.length == 1) {
            final OfflinePlayer target = PlayerManager.getOfflinePlayer(args[0]);
            if (target == null) {
                new MessageManager("errors.invalid-player")
                        .replace("%player%", args[0])
                        .send(player);
                return true;
            }
            new TrustManager(player, target).untrust();
            return true;
        }

        new MessageManager("errors.invalid-arguments").send(player);
        return true;
    }

    @SuppressWarnings("NullableProblems")
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        List<String> results = new ArrayList<>();
        Player player = (Player) sender;

        if (args.length == 1) for (UUID id : DataManager.trusted.get(player.getUniqueId())) suggestions.add(Bukkit.getOfflinePlayer(id).getName());
        for (String suggestion : suggestions) if (suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) results.add(suggestion);

        return results;
    }
}
