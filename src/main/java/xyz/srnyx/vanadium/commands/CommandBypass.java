package xyz.srnyx.vanadium.commands;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.managers.MessageManager;
import xyz.srnyx.vanadium.managers.PlayerManager;

import java.util.Objects;


public class CommandBypass implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (PlayerManager.noPermission(sender, "vanadium.bypass")) return true;
        final Player player = (Player) sender;

        // If a player is specified, enable bypass for them instead of sender
        if (args.length == 1 && player.hasPermission("vanadium.bypass.others") && !Objects.equals(args[0], player.getName())) {
            final Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                if (PlayerManager.hasScoreboardTag(target, "bypass")) {
                    target.removeScoreboardTag("bypass");
                    new MessageManager("bypass.disabled.self")
                            .send(target);
                    new MessageManager("bypass.disabled.other")
                            .replace("%target%", args[0])
                            .send(sender);
                } else {
                    enable(target, false);
                    new MessageManager("bypass.enabled.other")
                            .replace("%target%", args[0])
                            .send(sender);
                }
                return true;
            }
        }

        if (PlayerManager.hasScoreboardTag(player, "bypass")) {
            player.removeScoreboardTag("bypass");
            new MessageManager("bypass.disabled.self").send(player);
        } else enable(player, false);

        return true;
    }

    /**
     * Enables bypass
     *
     * @param   player  Player to enable bypass for
     * @param   join    Whether it was activated by the {@code player} joining
     */
    public static void enable(Player player, boolean join) {
        if (!join) {
            player.addScoreboardTag("bypass");
            new MessageManager("bypass.enabled.self").send(player);
        }

        new BukkitRunnable() {
            public void run() {
                if (!PlayerManager.isVanished(player)) player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(new MessageManager("bypass.actionbar").string()));
                if (!PlayerManager.hasScoreboardTag(player, "bypass")) cancel();
            }
        }.runTaskTimer(Main.plugin, 0, 40);
    }
}
