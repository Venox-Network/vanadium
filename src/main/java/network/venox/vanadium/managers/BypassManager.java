package network.venox.vanadium.managers;

import com.olliez4.interface4.util.ActionBar;

import network.venox.vanadium.Main;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class BypassManager {
    /**
     * Enables bypass
     *
     * @param   player  Player to enable bypass for
     * @param   join    Whether it was activated by the {@code player} joining
     */
    public void enable(Player player, boolean join) {
        if (!join) {
            player.addScoreboardTag("bypass");
            new MessageManager("bypass.enabled.self").send(player);
        }

        new BukkitRunnable() {public void run() {
            if (!PlayerManager.isVanished(player)) ActionBar.send(player, new MessageManager("bypass.actionbar").string());
            if (!PlayerManager.hasScoreboardTag(player, "bypass")) cancel();
        }}.runTaskTimer(Main.plugin, 0, 40);
    }
}
