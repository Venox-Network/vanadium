package xyz.srnyx.vanadium.managers;

import dev.lone.itemsadder.api.CustomStack;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import xyz.srnyx.vanadium.Main;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class ItemsAdderManager {
    private final Player player;
    public static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    public static final Map<UUID, ItemStack> axes = new ConcurrentHashMap<>();

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
                                    .replace("%seconds%", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) + 1))
                                    .string()));
                }
            }
        }.runTaskTimer(Main.plugin, 0, 20);
    }

    /**
     * Checks if the player is not on cooldown
     *
     * @return  True if the player is not on cooldown
     */
    public boolean notOnCooldown() {
        if (cooldowns.containsKey(player.getUniqueId())) {
            return (cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) <= 0;
        } else return true;
    }

    /**
     * Checks if a player is holding the specified custom item
     *
     * @param   item    The item's key name (without namespace)
     * @return          True if the player is holding the specified custom item
     */
    public boolean holdingItem(boolean offhand, String item) {
        final CustomStack mainCustom = CustomStack.byItemStack(player.getInventory().getItemInMainHand());
        if (offhand) {
            final CustomStack offCustom = CustomStack.byItemStack(player.getInventory().getItemInOffHand());
            return mainCustom != null && mainCustom.getId().equals(item) || offCustom != null && offCustom.getId().equals(item);
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
        final CustomStack mainCustom = CustomStack.byItemStack(player.getInventory().getItemInMainHand());
        if (mainCustom != null) {
            final int newDmg = mainCustom.getDurability() - durability;
            mainCustom.setDurability(newDmg);

            // Remove item if new durability is 0
            if (newDmg <= 0) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            }
        }

        if (offhand) {
            final CustomStack offCustom = CustomStack.byItemStack(player.getInventory().getItemInOffHand());
            if (offCustom != null) {
                final int newDmg = offCustom.getDurability() - durability;
                offCustom.setDurability(newDmg);

                // Remove item if new durability is 0
                if (newDmg <= 0) {
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                    player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                }
            }
        }
    }

    /**
     * Returns axe to the player
     *
     * @param   arrow   The arrow representing the axe
     */
    public void axe(Arrow arrow, int slot) {
        // Update durability of axe before returning it
        final CustomStack axe = CustomStack.byItemStack(axes.get(player.getUniqueId()));
        if (axe != null) {
            axe.setDurability(axe.getDurability() - 2);
            axes.put(player.getUniqueId(), axe.getItemStack());
        }

        // Return the axe and remove the arrow
        if (player.getInventory().getItem(slot) == null) {
            player.getInventory().setItem(slot, axes.get(player.getUniqueId()));
        } else {
            player.getInventory().addItem(axes.get(player.getUniqueId()));
        }
        axes.remove(player.getUniqueId());
        arrow.remove();
    }
}
