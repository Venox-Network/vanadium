package network.venox.vanadium.commands;

import network.venox.vanadium.managers.BypassManager;
import network.venox.vanadium.managers.MessageManager;
import network.venox.vanadium.managers.PlayerManager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class CommandBypass implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (PlayerManager.noPermission(sender, "vanadium.bypass")) return true;
        final Player player = (Player) sender;

        // If a player is specified, enable bypass for them instead of sender
        if (args.length == 1 && player.hasPermission("vanadium.bypass.others") && !Objects.equals(args[0], player.getName())) {
            final Player target = Bukkit.getPlayer(args[0]);
            if (target == null) return true;

            if (PlayerManager.hasScoreboardTag(target, "bypass")) {
                target.removeScoreboardTag("bypass");
                new MessageManager("bypass.disabled.self", cmd, args).send(target);
                new MessageManager("bypass.disabled.other", cmd, args)
                        .replace("%target%", args[0])
                        .send(sender);
                return true;
            }

            new BypassManager().enable(target, false);
            new MessageManager("bypass.enabled.other", cmd, args)
                    .replace("%target%", args[0])
                    .send(sender);
            return true;
        }

        if (PlayerManager.hasScoreboardTag(player, "bypass")) {
            player.removeScoreboardTag("bypass");
            new MessageManager("bypass.disabled.self", cmd, args).send(player);
            return true;
        }

        new BypassManager().enable(player, false);
        return true;
    }
}
