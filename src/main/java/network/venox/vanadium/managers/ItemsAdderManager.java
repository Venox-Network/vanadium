package network.venox.vanadium.managers;

import dev.lone.itemsadder.api.CustomStack;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import network.venox.vanadium.Main;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class ItemsAdderManager {
    private final Player player;
    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
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
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Main.config.getInt("custom-items.cooldown")));

        // Display action bar
        new BukkitRunnable() {
            public void run() {
                final String seconds = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) + 1);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                        new MessageManager("custom-items.action-bar")
                                .replace("%seconds%", seconds)
                                .string()));

                if (notOnCooldown()) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                    cancel();
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
        if (cooldowns.containsKey(player.getUniqueId())) return (cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) <= 0;
        return true;
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

    public Entity getTarget(int range) {
        final List<Entity> nearby = player.getNearbyEntities(range, range, range);
        final BlockIterator iterator = new BlockIterator(player, range);
        Block block;
        Location loc;
        int bx;
        int by;
        int bz;
        double ex;
        double ey;
        double ez;

        // loop through player's line of sight
        while (iterator.hasNext()) {
            block = iterator.next();
            bx = block.getX();
            by = block.getY();
            bz = block.getZ();

            // check for entities near this block in the line of sight
            for (Entity entity : nearby) {
                loc = entity.getLocation();
                ex = loc.getX();
                ey = loc.getY();
                ez = loc.getZ();

                if ((bx - 0.75 <= ex && ex <= bx + 1.75) && (bz - 0.75 <= ez && ez <= bz + 1.75) && (by - 1 <= ey && ey <= by + 2.5)) {
                    // entity is close enough, set target and stop
                    return entity;
                }
            }
        }

        return null;
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
        } else player.getInventory().addItem(axes.get(player.getUniqueId()));
        axes.remove(player.getUniqueId());
        arrow.remove();
    }
}
