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

    public ItemManager(Material material) {
        this.item = new ItemStack(material);
    }

    public ItemManager name(String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemManager lore(String lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> entries = new ArrayList<>();
            List<String> current = meta.getLore();
            if (current != null)
                entries.addAll(current);

            entries.add(ChatColor.translateAlternateColorCodes('&', lore));

            meta.setLore(entries);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemManager enchant(Enchantment enchantment, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemManager flag(ItemFlag flag) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(flag);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemStack get() {
        return item;
    }

    public static boolean isSame(ItemStack one, ItemStack two) {
        ItemMeta oneMeta = one.getItemMeta();
        ItemMeta twoMeta = two.getItemMeta();

        if (!one.getType().equals(two.getType())) return false;
        if (oneMeta != null && twoMeta != null) {
            if (!oneMeta.getDisplayName().equals(twoMeta.getDisplayName())) return false;
            //noinspection RedundantIfStatement
            if (oneMeta.getLore() != null && !oneMeta.getLore().equals(twoMeta.getLore())) return false;
        }

        return true;
    }
}
