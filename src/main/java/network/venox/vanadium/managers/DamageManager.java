package network.venox.vanadium.managers;

import dev.lone.itemsadder.api.CustomStack;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;


public class DamageManager {
    //TODO: Account for enchantments
    /**
     * Damages an entity and accounts for armor
     *
     * @param   entity  The entity to damage
     * @param   damage  The base damage to deal
     */
    public static void damage(LivingEntity entity, double damage) {
        final EntityEquipment inv = entity.getEquipment();
        if (inv == null) return;
        double red = 0.0;

        red += shield(inv, entity);
        red += helmet(inv);
        red += chestplate(inv);
        red += leggings(inv);
        red += boots(inv);

        entity.damage(damage - (damage * red));
    }

    /**
     * Damage reduction for shield
     *
     * @param   inv     The equipment of the entity
     * @param   entity  The entity being damaged
     *
     * @return          The amount of damage reduction
     */
    private static double shield(EntityEquipment inv, Entity entity) {
        if (inv.getItemInMainHand().getType() != Material.SHIELD && inv.getItemInOffHand().getType() != Material.SHIELD) return 0.0;
        if (!(entity instanceof Player player) || player.isBlocking()) return 0.05;
        return 0.0;
    }

    /**
     * Damage reduction for helmet
     *
     * @param   inv The equipment of the entity
     *
     * @return      The amount of damage reduction
     */
    private static double helmet(EntityEquipment inv) {
        if (inv.getHelmet() == null) return 0.0;

        final Material material = inv.getHelmet().getType();
        if (inv.getHelmet() == CustomStack.getInstance("vanadium_helmet").getItemStack()) return 0.16;
        if (material == Material.DIAMOND_HELMET || material == Material.NETHERITE_HELMET) return 0.12;
        if (material == Material.GOLDEN_HELMET || material == Material.CHAINMAIL_HELMET || material == Material.IRON_HELMET || material == Material.TURTLE_HELMET) return 0.08;
        if (material == Material.LEATHER_HELMET) return 0.04;

        return 0.0;
    }

    /**
     * Damage reduction for chestplate
     *
     * @param   inv The equipment of the entity
     *
     * @return      The amount of damage reduction
     */
    private static double chestplate(EntityEquipment inv) {
        if (inv.getChestplate() == null) return 0.0;

        final Material material = inv.getChestplate().getType();
        if (inv.getChestplate() == CustomStack.getInstance("vanadium_chestplate").getItemStack()) return 0.36;
        if (material == Material.DIAMOND_CHESTPLATE || material == Material.NETHERITE_CHESTPLATE) return 0.32;
        if (material == Material.GOLDEN_CHESTPLATE || material == Material.CHAINMAIL_CHESTPLATE || material == Material.IRON_CHESTPLATE) return 0.20;
        if (material == Material.LEATHER_CHESTPLATE) return 0.12;

        return 0.0;
    }

    /**
     * Damage reduction for leggings
     *
     * @param   inv The equipment of the entity
     *
     * @return      The amount of damage reduction
     */
    private static double leggings(EntityEquipment inv) {
        if (inv.getLeggings() != null) return 0.0;

        final Material material = inv.getLeggings().getType();
        if (inv.getLeggings() == CustomStack.getInstance("vanadium_leggings").getItemStack()) return 0.28;
        if (material == Material.DIAMOND_LEGGINGS || material == Material.NETHERITE_LEGGINGS) return 0.24;
        if (material == Material.IRON_LEGGINGS) return 0.20;
        if (material == Material.CHAINMAIL_LEGGINGS) return 0.16;
        if (material == Material.GOLDEN_LEGGINGS) return 0.12;
        if (material == Material.LEATHER_LEGGINGS) return 0.08;

        return 0.0;
    }

    /**
     * Damage reduction for boots
     *
     * @param   inv The equipment of the entity
     *
     * @return      The amount of damage reduction
     */
    private static double boots(EntityEquipment inv) {
        if (inv.getBoots() == null) return 0.0;

        final Material material = inv.getBoots().getType();
        if (inv.getBoots() == CustomStack.getInstance("vanadium_boots").getItemStack()) return 0.16;
        if (material == Material.DIAMOND_BOOTS || material == Material.NETHERITE_BOOTS) return 0.12;
        if (material == Material.IRON_BOOTS) return 0.08;
        if (material == Material.LEATHER_BOOTS || material == Material.GOLDEN_BOOTS || material == Material.CHAINMAIL_BOOTS) return 0.04;

        return 0.0;
    }
}
