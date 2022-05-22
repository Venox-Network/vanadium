package xyz.srnyx.vanadium.managers;

import org.apache.commons.lang.WordUtils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class TrustManager {
    private final Player player;
    private UUID targetId;
    private String targetName;

    /**
     * Constructor for {@code TrustManager}
     *
     * @param   op      Player trusted by {@code player} <i>(trusted)</i>
     * @param   player  Player that trusted {@code op} <i>(truster)</i>
     */
    public TrustManager(Player player, OfflinePlayer op) {
        this.player = player;
        if (op != null) {
            this.targetId = op.getUniqueId();
            this.targetName = op.getName();
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
    public boolean isTrusted(Block block) {
        final List<UUID> trusted = DataManager.trusted.get(targetId);
        final List<UUID> lockedTrusted = DataManager.lockedTrusted.get(block);
        return (trusted != null && trusted.contains(player.getUniqueId())) || (lockedTrusted != null && lockedTrusted.contains(player.getUniqueId()));
    }

    public void locked(Block block) {
        final UUID locker = new LockManager(block, player).getLocker();
        String playerString = "N/A";
        if (locker != null) playerString = Bukkit.getOfflinePlayer(locker).getName();

        new MessageManager("locking.block-locked")
                .replace("%block%", WordUtils.capitalizeFully(block.getType().name().replace("_", " ")))
                .replace("%player%", playerString)
                .send(player);
    }

    /**
     * Have {@code player} trust {@code opId}
     */
    public void trust() {
        if (new SlotManager("trusts", player).getCount() > getTrustedCount()) {
            final List<UUID> lockedTrusted = DataManager.trusted.get(player.getUniqueId());
            List<UUID> trusted = new ArrayList<>();
            if (lockedTrusted != null) trusted = new ArrayList<>(lockedTrusted);

            if (targetId != player.getUniqueId()) {
                if (!trusted.isEmpty()) {
                    if (!trusted.contains(targetId)) {
                        trusted.add(targetId);
                        DataManager.trusted.put(player.getUniqueId(), trusted);

                        new MessageManager("trusting.master.trust.success")
                                .replace("%player%", targetName)
                                .send(player);

                    } else new MessageManager("trusting.master.trust.fail")
                            .replace("%player%", targetName)
                            .send(player);

                } else {
                    DataManager.trusted.put(player.getUniqueId(), List.of(targetId));
                    new MessageManager("trusting.master.trust.success")
                            .replace("%player%", targetName)
                            .send(player);
                }

            } else new MessageManager("trusting.self").send(player);

        } else new MessageManager("slots.limit")
                    .replace("%type%", "trust")
                    .replace("%target%", targetName)
                    .replace("%total%", String.valueOf(new SlotManager("trusts", player).getCount())) //.replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1")
                    .send(player);
    }

    /**
     * Add {@code targetId} to the list of trusted players for {@code block}
     */
    public void trustBlock(Block block) {
        //noinspection DuplicatedCode
        final List<UUID> lockedTrusted = DataManager.lockedTrusted.get(block);
        List<UUID> trusted = new ArrayList<>();
        if (lockedTrusted != null) trusted = new ArrayList<>(lockedTrusted);
        final String blockName = WordUtils.capitalizeFully(block.getType().name().replace("_", " "));

        if (targetId != player.getUniqueId()) {
            if (!trusted.isEmpty()) {
                if (!trusted.contains(targetId)) {
                    trusted.add(targetId);
                    DataManager.lockedTrusted.put(block, trusted);

                    new MessageManager("trusting.block.trust.success")
                            .replace("%player%", targetName)
                            .replace("%block%", blockName)
                            .send(player);

                } else new MessageManager("trusting.block.trust.fail")
                        .replace("%player%", targetName)
                        .replace("%block%", blockName)
                        .send(player);

            } else {
                DataManager.lockedTrusted.put(block, List.of(targetId));
                new MessageManager("trusting.block.trust.success")
                        .replace("%player%", targetName)
                        .replace("%block%", blockName)
                        .send(player);
            }

        } else new MessageManager("trusting.self").send(player);
    }

    /**
     * Have {@code player} untrust {@code opId}
     */
    public void untrust() {
        final List<UUID> trustedList = DataManager.trusted.get(player.getUniqueId());
        List<UUID> trusted = new ArrayList<>();
        if (trustedList != null) trusted = new ArrayList<>(trustedList);

        if (!trusted.isEmpty() && trusted.contains(targetId)) {
            trusted.remove(targetId);
            DataManager.trusted.put(player.getUniqueId(), trusted);

            new MessageManager("trusting.master.untrust.success")
                    .replace("%player%", targetName)
                    .send(player);

        } else new MessageManager("trusting.master.untrust.fail")
                .replace("%player%", targetName)
                .send(player);
    }

    /**
     * Remove {@code targetId} from the list of trusted players for {@code block}
     */
    public void untrustBlock(Block block) {
        //noinspection DuplicatedCode
        final List<UUID> lockedTrusted = DataManager.lockedTrusted.get(block);
        List<UUID> trusted = new ArrayList<>();
        if (lockedTrusted != null) trusted = new ArrayList<>(lockedTrusted);
        final String blockName = WordUtils.capitalizeFully(block.getType().name().replace("_", " "));

        if (!trusted.isEmpty() && trusted.contains(targetId)) {
            trusted.remove(targetId);
            DataManager.lockedTrusted.put(block, trusted);

            new MessageManager("trusting.block.untrust.success")
                    .replace("%player%", targetName)
                    .replace("%block%", blockName)
                    .send(player);

        } else new MessageManager("trusting.block.untrust.fail")
                .replace("%player%", targetName)
                .replace("%block%", blockName)
                .send(player);
    }

    public void trustList(Block block) {
        List<UUID> trusted = DataManager.trusted.get(targetId);
        if (block != null) {
            trusted = DataManager.lockedTrusted.get(block);
            new MessageManager("trusting.block.list-header")
                    .replace("%player%", targetName)
                    .replace("%block%", WordUtils.capitalizeFully(block.getType().name().replace("_", " ")))
                    .send(player);
        } else new MessageManager("trusting.master.list-header")
                .replace("%player%", targetName)
                .send(player);

        if (trusted != null && !trusted.isEmpty()) {
            for (final UUID id : trusted) {
                final OfflinePlayer idPlayer = Bukkit.getOfflinePlayer(id);
                new MessageManager("trusting.list.item")
                        .replace("%player%", idPlayer.getName())
                        .send(player);
            }
        } else new MessageManager("trusting.list.empty").send(player);
    }
}
