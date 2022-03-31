package xyz.srnyx.vanadium.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.managers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class CommandUntrust implements TabExecutor {
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!Main.isPlayer(sender)) return true;
        if (!Main.hasPermission(sender, "vanadium.untrust")) return true;
        Player player = (Player) sender;

        if (args.length == 1) {
            OfflinePlayer target = new PlayerManager(args[0]).getOfflinePlayer();
            if (target == null) {
                new MessageManager("errors.invalid-player")
                        .replace("%player%", args[0])
                        .send(player);
                return true;
            }
            new TrustManager(target, player).untrust();
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
            for (String uuid : trusted) {
                suggestions.add(Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName());
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
