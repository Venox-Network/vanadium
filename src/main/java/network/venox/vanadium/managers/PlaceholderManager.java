package network.venox.vanadium.managers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import network.venox.vanadium.Main;
import network.venox.vanadium.managers.slots.LockSlotManager;
import network.venox.vanadium.managers.slots.TrustSlotManager;

import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;


public class PlaceholderManager extends PlaceholderExpansion {
    @Override
    public @NotNull String getAuthor() {
        return "srnyx";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "v";
    }

    @Override
    public @NotNull String getVersion() {
        return Main.plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        // %v_locked%
        if (params.equalsIgnoreCase("locked")) return String.valueOf(new LockManager(null, player).getLockedCount());

        // %v_trusted%
        if (params.equalsIgnoreCase("trusted")) return String.valueOf(new TrustManager(player, null).getTrustedCount());

        // %v_locks%
        if (params.equalsIgnoreCase("locks")) return String.valueOf(new LockSlotManager().getCount(player.getUniqueId()));

        final boolean afk = PlayerManager.isAFK(player);

        // %v_locks_time%
        if (params.equalsIgnoreCase("locks_time")) {
            if (new LockSlotManager(player).timeLeft() == null) return "N/A";
            return afk ? "N/A" : String.valueOf(TimeUnit.MILLISECONDS.toMinutes(new LockSlotManager(player).timeLeft()));
        }

        // %v_trusts%
        if (params.equalsIgnoreCase("trusts")) return String.valueOf(new TrustSlotManager().getCount(player.getUniqueId()));
        
        // %v_trusts_time%
        if (params.equalsIgnoreCase("trusts_time")) {
            if (new TrustSlotManager(player).timeLeft() == null) return "N/A";
            return afk ? "N/A" : String.valueOf(TimeUnit.MILLISECONDS.toMinutes(new TrustSlotManager(player).timeLeft()));
        }

        return null;
    }
}
