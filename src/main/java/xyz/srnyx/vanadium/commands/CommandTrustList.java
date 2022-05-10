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
        Player player = (Player) sender;

        if (args.length == 1 && (player.hasPermission("vanadium.trustlist.others"))) {
            Player targetPlayer = null;
            OfflinePlayer target = PlayerManager.getOfflinePlayer(args[0]);
            if (target != null) targetPlayer = Bukkit.getPlayer(target.getUniqueId());

            if (targetPlayer == null) {
                new MessageManager("errors.invalid-player")
                        .replace("%player%", args[0])
                        .send(player);
                return true;
            }

            trusted(player, targetPlayer);
            return true;
        }

        trusted(player, player);
        return true;
    }

    private void trusted(Player player, Player target) {
        List<UUID> trusted = DataManager.trusted.get(target.getUniqueId());
        new MessageManager("trusting.list.header")
                .replace("%player%", target.getName())
                .send(player);
        if (trusted.size() == 0) {
            new MessageManager("trusting.list.empty").send(player);
        } else {
            for (UUID id : trusted) {
                Player idPlayer = Bukkit.getPlayer(id);
                new MessageManager("trusting.list.item")
                        .replace("%player%", idPlayer != null ? idPlayer.getName() : null)
                        .send(player);
            }
        }
    }
}
