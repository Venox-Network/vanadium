package xyz.srnyx.vanadium.managers;

import dev.lone.itemsadder.api.CustomStack;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import xyz.srnyx.vanadium.Main;

import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class ItemsAdderManager {
    private final Player player;
    private final Map<UUID, Long> cooldown = new HashMap<>();
    public static final Map<UUID, ItemStack> axes = new HashMap<>();

    /**
     * Constructor for {@link ItemsAdderManager}
     *
     * @param   player  The player to use in the methods
     */
    public ItemsAdderManager(Player player) {
        this.player = player;
    }

    /**
     * Does the universal cooldown stuff
     */
    public void cooldown() {
        // Display action bar
        new BukkitRunnable() {
            public void run() {
                if (notOnCooldown()) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                    cancel();
                } else {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                            new MessageManager("custom-items.action-bar")
                                    .replace("%seconds%", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(cooldown.get(player.getUniqueId()) - System.currentTimeMillis()) + 1))
                                    .string()));
                }
            }
        }.runTaskTimer(Main.plugin, 0, 20);

        // Cooldown stuff
        cooldown.remove(player.getUniqueId());
        cooldown.put(player.getUniqueId(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Main.config.getInt("custom-items.cooldown")));
    }

    /**
     * Checks if the player is not on cooldown
     *
     * @return  True if the player is not on cooldown
     */
    public boolean notOnCooldown() {
        if (cooldown.containsKey(player.getUniqueId())) return (cooldown.get(player.getUniqueId()) - System.currentTimeMillis()) <= 0;
        return true;
    }

    /**
     * Checks if a player is holding the specified custom item
     *
     * @param   item    The item's key name (without namespace)
     * @return          True if the player is holding the specified custom item
     */
    public boolean holdingItem(boolean offhand, String item) {
        @Nullable CustomStack mainCustom = CustomStack.byItemStack(player.getInventory().getItemInMainHand());
        if (offhand) {
            @Nullable CustomStack offCustom = CustomStack.byItemStack(player.getInventory().getItemInOffHand());
            return (mainCustom != null && mainCustom.getId().equals(item)) || (offCustom != null && offCustom.getId().equals(item));
        }
        return mainCustom != null && mainCustom.getId().equals(item);
    }

    /**
     * Removes durability from the item the player is holding
     *
     * @param   offhand     Whether to check the offhand slot or not
     * @param   durability  The amount of durability to negate
     */
    public void durability(boolean offhand, int durability) {
        @Nullable CustomStack mainCustom = CustomStack.byItemStack(player.getInventory().getItemInMainHand());
        if (mainCustom != null) mainCustom.setDurability(mainCustom.getDurability() - durability);
        if (offhand) {
            @Nullable CustomStack offCustom = CustomStack.byItemStack(player.getInventory().getItemInOffHand());
            if (offCustom != null) offCustom.setDurability(offCustom.getDurability() - durability);
        }
    }

    /**
     * Returns axe to the player
     *
     * @param   arrow   The arrow representing the axe
     */
    public static void axe(Player player, Arrow arrow) {
        // Update durability of axe before returning it
        @Nullable CustomStack axe = CustomStack.byItemStack(axes.get(player.getUniqueId()));
        if (axe != null) {
            axe.setDurability(axe.getDurability() - 2);
            axes.put(player.getUniqueId(), axe.getItemStack());
        }

        // Return the axe and remove the arrow
        player.getInventory().addItem(axes.get(player.getUniqueId()));
        axes.remove(player.getUniqueId());
        arrow.remove();
    }
}
