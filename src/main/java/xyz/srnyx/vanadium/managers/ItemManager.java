package xyz.srnyx.vanadium.managers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;


public class ItemManager {
    final ItemStack item;

    /**
     * Creates a new ItemManager with the given material
     *
     * @param   material    The material to create the item with
     */
    public ItemManager(Material material) {
        this.item = new ItemStack(material);
    }

    /**
     * Sets the item's name
     *
     * @param   name    The name to give the item
     *
     * @return          The ItemManager instance
     */
    public ItemManager name(String name) {
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            item.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Gives the item lore
     *
     * @param   lore    The lore to give the item
     *
     * @return          The ItemManager instance
     */
    public ItemManager lore(String lore) {
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            final List<String> entries = new ArrayList<>();
            final List<String> current = meta.getLore();
            if (current != null) entries.addAll(current);

            entries.add(ChatColor.translateAlternateColorCodes('&', lore));

            meta.setLore(entries);
            item.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Adds an enchantment to the item
     *
     * @param   enchantment The enchantment to add
     * @param   level       The level of the enchantment
     *
     * @return              The ItemManager instance
     */
    public ItemManager enchant(Enchantment enchantment, int level) {
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
            item.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Adds an item flag to the item
     *
     * @param   flag    The item flag to add
     *
     * @return          The ItemManager instance
     */
    public ItemManager flag(ItemFlag flag) {
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(flag);
            item.setItemMeta(meta);
        }
        return this;
    }

    /**
     * @return  The item
     */
    public ItemStack get() {
        return item;
    }
}
