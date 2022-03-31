package xyz.srnyx.vanadium.managers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;


public class PlaceholderManager extends PlaceholderExpansion {

    public PlaceholderManager() {}

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
        return "0.0.1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (params.equalsIgnoreCase("blocks")) {
            return String.valueOf(new LockManager().getLockedCount(player));
        }
        if (params.equalsIgnoreCase("slots_locks")) {
            return String.valueOf(new SlotManager("locks", player).getCount());
        }
        if (params.equalsIgnoreCase("slots_locks_time")) {
            return String.valueOf(TimeUnit.MILLISECONDS.toMinutes(new SlotManager("locks", player).timeLeft()));
        }

        if (params.equalsIgnoreCase("trusted")) {
            return String.valueOf(new TrustManager(player).getTrustedCount());
        }
        if (params.equalsIgnoreCase("slots_trusts")) {
            return String.valueOf(new SlotManager("trusts", player).getCount());
        }
        if (params.equalsIgnoreCase("slots_trusts_time")) {
            return String.valueOf(TimeUnit.MILLISECONDS.toMinutes(new SlotManager("trusts", player).timeLeft()));
        }
        return null;
    }
}
