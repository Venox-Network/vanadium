package xyz.srnyx.vanadium.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.managers.MessageManager;
import xyz.srnyx.vanadium.managers.PlayerManager;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class CommandTrustList implements CommandExecutor {
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!new PlayerManager(sender).hasPermission("vanadium.trustlist")) return true;
        Player player = (Player) sender;

        if (args.length == 1 && (player.hasPermission("vanadium.trustlist.others"))) {
            OfflinePlayer target = new PlayerManager(args[0]).getOfflinePlayer();
            trusted(player, Objects.requireNonNullElse(target.getUniqueId(), player.getUniqueId()));
            return true;
        }
        trusted(player, player.getUniqueId());
        return true;
    }

    private void trusted(Player player, UUID target) {
        List<String> trusted = Main.trusted.getStringList(target.toString());
        //noinspection ConstantConditions
        new MessageManager("trusting.list.header")
                .replace("%player%", Bukkit.getPlayer(target).getName())
                .send(player);
        if (trusted.size() == 0) {
            new MessageManager("trusting.list.empty").send(player);
        } else {
            for (String uuid : trusted) {
                new MessageManager("trusting.list.item")
                        .replace("%player%", Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName())
                        .send(player);
            }
        }
    }
}
