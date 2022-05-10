package xyz.srnyx.vanadium.managers;

import com.earth2me.essentials.Essentials;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.Bukkit;
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
        return "0.0.2";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

        // %v_locked%
        if (params.equalsIgnoreCase("locked")) return String.valueOf(new LockManager(null, player).getLockedCount());
        // %v_trusted%
        if (params.equalsIgnoreCase("trusted")) return String.valueOf(new TrustManager(player, null).getTrustedCount());


        // %v_locks%
        if (params.equalsIgnoreCase("locks")) return String.valueOf(new SlotManager("locks", player).getCount());
        // %v_locks_gain%
        if (params.equalsIgnoreCase("locks_gain")) {
            if (essentials != null && essentials.getUser(player).isAfk()) {
                return "false";
            } else return String.valueOf(new SlotManager("locks", player).contains());
        }
        // %v_locks_time%
        if (params.equalsIgnoreCase("locks_time")) {
            if (new SlotManager("locks", player).timeLeft(true) != null) {
                String value = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(new SlotManager("locks", player).timeLeft(true)));
                if (essentials != null) {
                    if (!essentials.getUser(player).isAfk()) {
                        return value;
                    } else return "N/A";
                } else return value;
            } else return "N/A";
        }

        // %v_trusts%
        if (params.equalsIgnoreCase("trusts")) return String.valueOf(new SlotManager("trusts", player).getCount());
        // %v_trusts_gain%
        if (params.equalsIgnoreCase("trusts_gain")) {
            if (essentials != null && essentials.getUser(player).isAfk()) {
                return "false";
            } else return String.valueOf(new SlotManager("trusts", player).contains());
        }
        // %v_trusts_time%
        if (params.equalsIgnoreCase("trusts_time")) {
            if (new SlotManager("trusts", player).timeLeft(true) != null) {
                String value = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(new SlotManager("trusts", player).timeLeft(true)));
                if (essentials != null) {
                    if (!essentials.getUser(player).isAfk()) {
                        return value;
                    } else return "N/A";
                } else return value;
            } else return "N/A";
        }

        return null;
    }
}
