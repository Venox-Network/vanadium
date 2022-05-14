package xyz.srnyx.vanadium.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.srnyx.vanadium.managers.DataManager;
import xyz.srnyx.vanadium.managers.MessageManager;
import xyz.srnyx.vanadium.managers.PlayerManager;

import java.util.List;
import java.util.UUID;


public class CommandTrustList implements CommandExecutor {
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (PlayerManager.noPermission(sender, "vanadium.trustlist")) return true;
        final Player player = (Player) sender;

        if (args.length == 1 && player.hasPermission("vanadium.trustlist.others")) {
            final OfflinePlayer target = PlayerManager.getOfflinePlayer(args[0]);

            if (target != null) {
                trusted(player, target);
            } else {
                new MessageManager("errors.invalid-player")
                        .replace("%player%", args[0])
                        .send(player);
            }

            return true;
        }

        trusted(player, Bukkit.getOfflinePlayer(player.getUniqueId()));
        return true;
    }

    private void trusted(Player player, OfflinePlayer target) {
        final List<UUID> trusted = DataManager.trusted.get(target.getUniqueId());

        new MessageManager("trusting.list.header")
                .replace("%player%", target.getName())
                .send(player);

        if (trusted != null && !trusted.isEmpty()) {
            for (final UUID id : trusted) {
                final OfflinePlayer idPlayer = Bukkit.getOfflinePlayer(id);
                new MessageManager("trusting.list.item")
                        .replace("%player%", idPlayer.getName())
                        .send(player);
            }
        } else {
            new MessageManager("trusting.list.empty").send(player);
        }
    }
}
