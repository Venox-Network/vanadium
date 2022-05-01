package xyz.srnyx.vanadium.commands;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.managers.PlayerManager;
import xyz.srnyx.vanadium.managers.MessageManager;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class CommandBypass implements CommandExecutor {
    private static final String tag = "bypass";

    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!new PlayerManager(sender).isPlayer()) return true;
        if (!new PlayerManager(sender).hasPermission("vanadium.bypass")) return true;
        Player player = (Player) sender;

        if (new PlayerManager(player).hasScoreboardTag(tag)) {
            player.removeScoreboardTag(tag);
            new MessageManager("locking.bypass.disabled").send(player);
        } else {
            enable(player, false);
        }
        return true;
    }

    public static void enable(Player player, boolean join) {
        if (!join) {
            player.addScoreboardTag(tag);
            new MessageManager("locking.bypass.enabled").send(player);
        }

        new BukkitRunnable() {
            public void run() {
                if (!new PlayerManager(player).isVanished()) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(new MessageManager("locking.bypass.actionbar").toString()));
                }
                if (!check(player)) {
                    cancel();
                }
            }
        }.runTaskTimer(Main.plugin, 0, 40);
    }

    public static boolean check(Player player) {
        return (new PlayerManager(player).hasScoreboardTag(tag));
    }
}
