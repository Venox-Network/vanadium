package xyz.srnyx.vanadium.managers;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;


public class DamageManager {
    @SuppressWarnings("ConstantConditions")
    public static void damage(LivingEntity entity, double dmg) {
        EntityEquipment inv = entity.getEquipment();
        double red = 0.0;

        if (inv.getItemInMainHand().getType() != Material.SHIELD) {
            red = red + 0.0;
        } else if (entity instanceof Player player) {
            if (player.isBlocking()) {
                red = red + 0.05;
            }
        } else {
            red = red + 0.05;
        }

        if (inv.getItemInOffHand().getType() != Material.SHIELD) {
            red = red + 0.0;
        } else if (entity instanceof Player player) {
            if (player.isBlocking()) {
                red = red + 0.05;
            }
        } else {
            red = red + 0.05;
        }

        ItemStack helmet = inv.getHelmet();
        if (inv.getHelmet() != null) {
            Material helmetType = helmet.getType();
            if (helmetType == Material.LEATHER_HELMET) {
                red = red + 0.04;
            } else if (helmetType == Material.GOLDEN_HELMET || helmetType == Material.CHAINMAIL_HELMET || helmetType == Material.IRON_HELMET) {
                red = red + 0.08;
            } else if (helmetType == Material.DIAMOND_HELMET || helmetType == Material.NETHERITE_HELMET) {
                red = red + 0.12;
            } else {
                red += 0.0;
            }
        }

        ItemStack chest = inv.getChestplate();
        if (inv.getChestplate() != null) {
            Material chestType = chest.getType();
            if (chestType == Material.LEATHER_CHESTPLATE) {
                red = red + 0.12;
            } else if (chestType == Material.GOLDEN_CHESTPLATE || chestType == Material.CHAINMAIL_CHESTPLATE || chestType == Material.IRON_CHESTPLATE) {
                red = red + 0.20;
            } else if (chestType == Material.DIAMOND_CHESTPLATE || chestType == Material.NETHERITE_CHESTPLATE) {
                red = red + 0.32;
            } else {
                red += 0.0;
            }
        }

        ItemStack pants = inv.getLeggings();
        if (inv.getLeggings() != null) {
            Material pantsType = pants.getType();
            if (pants.getType() == Material.LEATHER_LEGGINGS) {
                red = red + 0.08;
            } else if (pantsType == Material.GOLDEN_LEGGINGS) {
                red = red + 0.12;
            } else if (pantsType == Material.CHAINMAIL_LEGGINGS) {
                red = red + 0.16;
            } else if (pantsType == Material.IRON_LEGGINGS) {
                red = red + 0.20;
            } else if (pantsType == Material.DIAMOND_LEGGINGS || pantsType == Material.NETHERITE_LEGGINGS) {
                red = red + 0.24;
            } else {
                red += 0.0;
            }
        }

        ItemStack boots = inv.getBoots();
        if (inv.getBoots() != null) {
            Material bootsType = boots.getType();
            if (bootsType == Material.LEATHER_BOOTS || bootsType == Material.GOLDEN_BOOTS || bootsType == Material.CHAINMAIL_BOOTS) {
                red = red + 0.04;
            } else if (bootsType == Material.IRON_BOOTS) {
                red = red + 0.08;
            } else if (bootsType == Material.DIAMOND_BOOTS || bootsType == Material.NETHERITE_BOOTS) {
                red = red + 0.12;
            } else {
                red += 0.0;
            }
        }

        entity.damage(dmg - (dmg * red));
    }
}
