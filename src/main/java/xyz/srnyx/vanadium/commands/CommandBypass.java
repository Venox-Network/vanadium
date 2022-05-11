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
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (PlayerManager.isNotPlayer(sender)) return true;
        if (PlayerManager.noPermission(sender, "vanadium.bypass")) return true;
        Player player = (Player) sender;

        if (PlayerManager.hasScoreboardTag(player, "bypass")) {
            player.removeScoreboardTag("bypass");
            new MessageManager("locking.bypass.disabled").send(player);
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
            new MessageManager("locking.bypass.enabled").send(player);
        }

        new BukkitRunnable() {
            public void run() {
                if (!PlayerManager.isVanished(player)) player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(new MessageManager("locking.bypass.actionbar").toString()));
                if (!PlayerManager.hasScoreboardTag(player, "bypass")) cancel();
            }
        }.runTaskTimer(Main.plugin, 0, 40);
    }
}
