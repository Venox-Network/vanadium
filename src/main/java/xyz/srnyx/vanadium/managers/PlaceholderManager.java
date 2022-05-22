package xyz.srnyx.vanadium.managers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.vanadium.Main;
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
        if (params.equalsIgnoreCase("locks")) return String.valueOf(new SlotManager("locks", player).getCount());

        final boolean afk = PlayerManager.isAFK(player);

        // %v_locks_time%
        if (params.equalsIgnoreCase("locks_time")) {
            if (new SlotManager("locks", player).timeLeft() != null) {
                return afk ? "N/A" : String.valueOf(TimeUnit.MILLISECONDS.toMinutes(new SlotManager("locks", player).timeLeft()));
            }
            return "N/A";
        }

        // %v_trusts%
        if (params.equalsIgnoreCase("trusts")) return String.valueOf(new SlotManager("trusts", player).getCount());
        
        // %v_trusts_time%
        if (params.equalsIgnoreCase("trusts_time")) {
            if (new SlotManager("trusts", player).timeLeft() != null) {
                return afk ? "N/A" : String.valueOf(TimeUnit.MILLISECONDS.toMinutes(new SlotManager("trusts", player).timeLeft()));
            }
            return "N/A";
        }

        return null;
    }
}
