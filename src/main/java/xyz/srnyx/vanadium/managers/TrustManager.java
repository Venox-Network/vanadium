package xyz.srnyx.vanadium.managers;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;


public class TrustManager {
    private final Player player;
    private UUID opId;
    private String opName;

    /**
     * Constructor for {@code TrustManager}
     *
     * @param   op      Player trusted by {@code player} <i>(trusted)</i>
     * @param   player  Player that trusted {@code op} <i>(truster)</i>
     */
    public TrustManager(Player player, OfflinePlayer op) {
        this.player = player;
        if (op != null) {
            this.opId = op.getUniqueId();
            this.opName = op.getName();
        }
    }

    /**
     * Get the number of players {@code player} has trusted
     */
    public int getTrustedCount() {
        final List<UUID> list = DataManager.trusted.get(player.getUniqueId());
        return list != null ? list.size() : 0;
    }

    /**
     * Check if {@code player} is trusted by {@code opId}
     *
     * @return  True if trusted, false if not
     */
    public boolean isTrusted() {
        return DataManager.trusted.get(opId).contains(player.getUniqueId());
    }

    /**
     * Have {@code player} trust {@code opId}
     */
    public void trust() {
        if (new SlotManager("trusts", player).getCount() > getTrustedCount()) {
            final List<UUID> trusted = DataManager.trusted.get(player.getUniqueId());
            if (trusted != null && !trusted.contains(opId) && opId != player.getUniqueId()) {
                trusted.add(opId);
                DataManager.trusted.put(player.getUniqueId(), trusted);
                new MessageManager("trusting.trust.success")
                        .replace("%player%", opName)
                        .send(player);
            } else {
                new MessageManager("trusting.trust.fail")
                        .replace("%player%", opName)
                        .send(player);
            }
        } else {
            new MessageManager("slots.limit")
                    .replace("%type%", "trust")
                    .replace("%target%", opName)
                    .replace("%total%", String.valueOf(new SlotManager("trusts", player).getCount())) //.replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1")
                    .send(player);
        }
    }

    /**
     * Have {@code player} untrust {@code opId}
     */
    public void untrust() {
        final List<UUID> trusted = DataManager.trusted.get(player.getUniqueId());
        if (trusted != null && trusted.contains(opId)) {
            trusted.remove(opId);
            DataManager.trusted.put(player.getUniqueId(), trusted);
            new MessageManager("trusting.untrust.success")
                    .replace("%player%", opName)
                    .send(player);
        } else {
            new MessageManager("trusting.untrust.fail")
                    .replace("%player%", opName)
                    .send(player);
        }
    }
}
