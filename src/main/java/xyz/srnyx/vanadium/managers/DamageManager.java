package xyz.srnyx.vanadium.managers;

import dev.lone.itemsadder.api.CustomStack;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;


public class DamageManager {
    //TODO: Account for enchantments
    /**
     * Damages an entity and accounts for armor
     *
     * @param   entity  The entity to damage
     * @param   dmg     The base damage to deal
     */
    public static void damage(LivingEntity entity, double dmg) {
        final EntityEquipment inv = entity.getEquipment();
        if (inv == null) return;
        double red = 0.0;

        if (inv.getItemInMainHand().getType() == Material.SHIELD) {
            if (entity instanceof Player player) {
                if (player.isBlocking()) {
                    red += 0.05;
                }
            } else {
                red += 0.05;
            }
        } else {
            red += 0.05;
        }

        if (inv.getItemInOffHand().getType() != Material.SHIELD) {
            red += 0.0;
        } else if (entity instanceof Player player) {
            if (player.isBlocking()) {
                red += 0.05;
            }
        } else {
            red += 0.05;
        }

        if (inv.getHelmet() != null) {
            final Material helmet = inv.getHelmet().getType();
            if (helmet == Material.LEATHER_HELMET) {
                red += 0.04;
            } else if (helmet == Material.GOLDEN_HELMET || helmet == Material.CHAINMAIL_HELMET || helmet == Material.IRON_HELMET || helmet == Material.TURTLE_HELMET) {
                red += 0.08;
            } else if (helmet == Material.DIAMOND_HELMET || helmet == Material.NETHERITE_HELMET) {
                red += 0.12;
            } else if (inv.getHelmet() == CustomStack.getInstance("vanadium_helmet").getItemStack()) {
                red += 0.16;
            } else {
                red += 0.0;
            }
        }

        if (inv.getChestplate() != null) {
            final Material chestplate = inv.getChestplate().getType();
            if (chestplate == Material.LEATHER_CHESTPLATE) {
                red += 0.12;
            } else if (chestplate == Material.GOLDEN_CHESTPLATE || chestplate == Material.CHAINMAIL_CHESTPLATE || chestplate == Material.IRON_CHESTPLATE) {
                red += 0.20;
            } else if (chestplate == Material.DIAMOND_CHESTPLATE || chestplate == Material.NETHERITE_CHESTPLATE) {
                red += 0.32;
            } else if (inv.getChestplate() == CustomStack.getInstance("vanadium_chestplate").getItemStack()) {
                red += 0.16;
            } else {
                red += 0.0;
            }
        }

        if (inv.getLeggings() != null) {
            final Material leggings = inv.getLeggings().getType();
            if (leggings == Material.LEATHER_LEGGINGS) {
                red += 0.08;
            } else if (leggings == Material.GOLDEN_LEGGINGS) {
                red += 0.12;
            } else if (leggings == Material.CHAINMAIL_LEGGINGS) {
                red += 0.16;
            } else if (leggings == Material.IRON_LEGGINGS) {
                red += 0.20;
            } else if (leggings == Material.DIAMOND_LEGGINGS || leggings == Material.NETHERITE_LEGGINGS) {
                red += 0.24;
            } else if (inv.getLeggings() == CustomStack.getInstance("vanadium_helmet").getItemStack()) {
                red += 0.16;
            } else {
                red += 0.0;
            }
        }

        if (inv.getBoots() != null) {
            final Material boots = inv.getBoots().getType();
            if (boots == Material.LEATHER_BOOTS || boots == Material.GOLDEN_BOOTS || boots == Material.CHAINMAIL_BOOTS) {
                red += 0.04;
            } else if (boots == Material.IRON_BOOTS) {
                red += 0.08;
            } else if (boots == Material.DIAMOND_BOOTS || boots == Material.NETHERITE_BOOTS) {
                red += 0.12;
            } else if (inv.getBoots() == CustomStack.getInstance("vanadium_boots").getItemStack()) {
                red += 0.16;
            } else {
                red += 0.0;
            }
        }

        entity.damage(dmg - (dmg * red));
    }
}
