package network.venox.vanadium.managers;

import network.venox.vanadium.managers.slots.TrustSlotManager;

import org.apache.commons.lang.WordUtils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
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

    /**
     * Send the "block is locked" message to {@code player}
     *
     * @param   block   Block that is locked
     */
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
     * Send the "successfully trusted" message to {@code player}
     *
     */
    public void success(String action, Block block) {
        Sound sound = Sound.ENTITY_ARROW_HIT_PLAYER;
        String type = "master";
        String blockName = "";
        if (action.equals("untrust")) sound = Sound.ENTITY_ITEM_BREAK;
        if (block != null) {
            type = "block";
            blockName = WordUtils.capitalizeFully(block.getType().name().replace("_", " "));
        }

        player.playSound(player.getLocation(), sound, 1, 2);
        new MessageManager("trusting." + type + "." + action + ".success")
                .replace("%player%", targetName)
                .replace("%block%", blockName)
                .send(player);
    }

    /**
     * Have {@code player} trust {@code opId}
     */
    public void trustPlayer() {
        final List<UUID> lockedTrusted = DataManager.trusted.get(player.getUniqueId());
        List<UUID> trusted = new ArrayList<>();
        if (lockedTrusted != null) trusted = new ArrayList<>(lockedTrusted);

        if (new TrustSlotManager(player).getCount(player.getUniqueId()) <= getTrustedCount()) {
            new MessageManager("slots.limit")
                    .replace("%type%", "trust")
                    .replace("%target%", targetName)
                    .replace("%total%", String.valueOf(new TrustSlotManager().getCount(player.getUniqueId()))) //.replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1")
                    .send(player);
            return;
        }

        if (targetId == player.getUniqueId()) {
            new MessageManager("trusting.self").send(player);
            return;
        }

        if (trusted.isEmpty()) {
            DataManager.trusted.put(player.getUniqueId(), List.of(targetId));
            success("trust", null);
            return;
        }

        if (trusted.contains(targetId)) {
            new MessageManager("trusting.master.trust.fail")
                .replace("%player%", targetName)
                .send(player);
            return;
        }

        trusted.add(targetId);
        DataManager.trusted.put(player.getUniqueId(), trusted);
        success("trust", null);
    }

    /**
     * Add {@code targetId} to the list of trusted players for {@code block}
     */
    public void trustBlock(Block block, boolean two) {
        //noinspection DuplicatedCode
        final List<UUID> lockedTrusted = DataManager.lockedTrusted.get(block);
        List<UUID> trusted = new ArrayList<>();
        if (lockedTrusted != null) trusted = new ArrayList<>(lockedTrusted);

        if (targetId == player.getUniqueId()) {
            new MessageManager("trusting.self").send(player);
            return;
        }

        if (trusted.isEmpty()) {
            DataManager.lockedTrusted.put(block, List.of(targetId));
            if (!two) success("trust", block);
            return;
        }

        if (trusted.contains(targetId)) {
            new MessageManager("trusting.block.trust.fail")
                    .replace("%player%", targetName)
                    .replace("%block%", WordUtils.capitalizeFully(block.getType().name().replace("_", " ")))
                    .send(player);
            return;
        }

        trusted.add(targetId);
        DataManager.lockedTrusted.put(block, trusted);
        if (!two) success("trust", block);
    }

    /**
     * Have {@code player} untrust {@code opId}
     */
    public void untrustPlayer() {
        final List<UUID> trustedList = DataManager.trusted.get(player.getUniqueId());
        List<UUID> trusted = new ArrayList<>();
        if (trustedList != null) trusted = new ArrayList<>(trustedList);

        if (trusted.isEmpty() || !trusted.contains(targetId)) {
            new MessageManager("trusting.master.untrust.fail")
                    .replace("%player%", targetName)
                    .send(player);
            return;
        }

        trusted.remove(targetId);
        DataManager.trusted.put(player.getUniqueId(), trusted);
        success("untrust", null);
    }

    /**
     * Remove {@code targetId} from the list of trusted players for {@code block}
     */
    public void untrustBlock(Block block, boolean two) {
        //noinspection DuplicatedCode
        final List<UUID> lockedTrusted = DataManager.lockedTrusted.get(block);
        List<UUID> trusted = new ArrayList<>();
        if (lockedTrusted != null) trusted = new ArrayList<>(lockedTrusted);
        final String blockName = WordUtils.capitalizeFully(block.getType().name().replace("_", " "));

        if (trusted.isEmpty() || !trusted.contains(targetId)) {
            new MessageManager("trusting.block.untrust.fail")
                .replace("%player%", targetName)
                .replace("%block%", blockName)
                .send(player);
            return;
        }

        trusted.remove(targetId);
        DataManager.lockedTrusted.put(block, trusted);
        if (!two) success("untrust", block);
    }

    /**
     * Send the header and items for the trust list
     *
     * @param   block   The block to display the trust list for ({@code null} for master list)
     */
    public void trustList(Block block) {
        listHeader(block);
        listItem(block);
    }

    /**
     * Send the header for the trust list
     *
     * @param   block   The block to display the header of ({@code null} for master list)
     */
    private void listHeader(Block block) {
        if (block == null) {
            new MessageManager("trusting.master.list-header")
                    .replace("%player%", targetName)
                    .send(player);
            return;
        }

        new MessageManager("trusting.block.list-header")
                .replace("%player%", targetName)
                .replace("%block%", WordUtils.capitalizeFully(block.getType().name().replace("_", " ")))
                .send(player);
    }

    /**
     * Send the items for the trust list
     *
     * @param   block   The block to display the items of ({@code null} for master list)
     */
    private void listItem(Block block) {
        List<UUID> trusted = DataManager.trusted.get(targetId);
        if (block != null) trusted = DataManager.lockedTrusted.get(block);

        if (trusted == null || trusted.isEmpty()) {
            new MessageManager("trusting.list.empty").send(player);
            return;
        }

        for (final UUID id : trusted) {
            final OfflinePlayer idPlayer = Bukkit.getOfflinePlayer(id);
            new MessageManager("trusting.list.item")
                    .replace("%player%", idPlayer.getName())
                    .send(player);
        }
    }
}
