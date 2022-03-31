package xyz.srnyx.vanadium.managers;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import xyz.srnyx.vanadium.Main;

import java.util.List;


@SuppressWarnings({"FieldMayBeFinal", "CanBeFinal"})
public class TrustManager {
    private OfflinePlayer op;
    private Player player;

    private String opId;
    private String playerId;

    /**
     * Constructor for {@code TrustManager}
     *
     * @param   op      Player trusted by {@code player} <i>(trusted)</i>
     * @param   player  Player that trusted {@code op} <i>(truster)</i>
     */
    public TrustManager(OfflinePlayer op, Player player) {
        this.op = op;
        this.player = player;

        this.opId = op.getUniqueId().toString();
        this.playerId = player.getUniqueId().toString();
    }

    /**
     * Constructor for {@code TrustManager} without {@code op}
     *
     * @param   player  Player to use
     */
    public TrustManager(Player player) {
        this.player = player;

        this.playerId = player.getUniqueId().toString();
    }

    /**
     * Get the number of players {@code player} has trusted
     */
    public int getTrustedCount() {
        return Main.trusted.getStringList(playerId).size();
    }

    /**
     * Check if {@code player} is trusted by {@code op}
     *
     * @return  True if trusted, false if not
     */
    public boolean isTrusted() {
        List<String> trusted = Main.trusted.getStringList(opId);
        return trusted.contains(playerId);
    }

    /**
     * Have {@code player} trust {@code op}
     */
    public void trust() {
        List<String> trusted = Main.trusted.getStringList(playerId);
        if (new SlotManager("trusts", player).getCount() > getTrustedCount()) {
            if (!trusted.contains(opId) && op.getUniqueId() != player.getUniqueId()) {
                trusted.add(opId);
                Main.trusted.set(playerId, trusted);
                save();
                new MessageManager("trusting.trust.success")
                        .replace("%player%", op.getName())
                        .send(player);
            } else {
                new MessageManager("trusting.trust.fail")
                        .replace("%player%", op.getName())
                        .send(player);
            }
        } else {
            String type = "trusts";
            new MessageManager("slots.limit")
                    .replace("%type%", type.substring(0, type.length() - 1))
                    .replace("%target%", op.getName())
                    .replace("%total%", String.valueOf(Main.slots.getInt(player.getUniqueId() + "." + type)).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                    .send(player);
        }
    }

    /**
     * Have {@code player} untrust {@code op}
     */
    public void untrust() {
        List<String> trusted = Main.trusted.getStringList(playerId);
        if (trusted.contains(opId)) {
            trusted.remove(opId);
            Main.trusted.set(playerId, trusted);
            save();
            new MessageManager("trusting.untrust.success")
                    .replace("%player%", op.getName())
                    .send(player);
        } else {
            new MessageManager("trusting.untrust.fail")
                    .replace("%player%", op.getName())
                    .send(player);
        }
    }

    /**
     * Saves {@code trusted.yml}
     */
    private void save() {
        new ConfigManager("trusted.yml", true).saveData(Main.trusted);
    }
}
