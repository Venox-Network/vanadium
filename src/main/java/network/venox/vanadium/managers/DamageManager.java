package network.venox.vanadium.managers;

import dev.lone.itemsadder.api.CustomStack;

import network.venox.vanadium.Main;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;


public class DamageManager {
    private final boolean ia = Main.itemsAdderInstalled();

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

        final DamageManager dm = new DamageManager();
        red += dm.shield(inv, entity);
        red += dm.helmet(inv);
        red += dm.chestplate(inv);
        red += dm.leggings(inv);
        red += dm.boots(inv);

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
    private double shield(EntityEquipment inv, LivingEntity entity) {
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
    private double helmet(EntityEquipment inv) {
        if (inv.getHelmet() == null) return 0.0;

        if (ia) {
            final CustomStack custom = CustomStack.getInstance("vanadium_helmet");
            if (custom != null && inv.getHelmet() == custom.getItemStack()) return 0.16;
        }

        final Material material = inv.getHelmet().getType();
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
    private double chestplate(EntityEquipment inv) {
        if (inv.getChestplate() == null) return 0.0;

        if (ia) {
            final CustomStack custom = CustomStack.getInstance("vanadium_chestplate");
            if (custom != null && inv.getChestplate() == custom.getItemStack()) return 0.36;
        }

        final Material material = inv.getChestplate().getType();
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
    private double leggings(EntityEquipment inv) {
        if (inv.getLeggings() == null) return 0.0;

        if (ia) {
            final CustomStack custom = CustomStack.getInstance("vanadium_leggings");
            if (custom != null && inv.getLeggings() == custom.getItemStack()) return 0.28;
        }

        final Material material = inv.getLeggings().getType();
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
    private double boots(EntityEquipment inv) {
        if (inv.getBoots() == null) return 0.0;

        if (ia) {
            final CustomStack custom = CustomStack.getInstance("vanadium_boots");
            if (custom != null && inv.getBoots() == custom.getItemStack()) return 0.16;
        }

        final Material material = inv.getBoots().getType();
        if (material == Material.DIAMOND_BOOTS || material == Material.NETHERITE_BOOTS) return 0.12;
        if (material == Material.IRON_BOOTS) return 0.08;
        if (material == Material.LEATHER_BOOTS || material == Material.GOLDEN_BOOTS || material == Material.CHAINMAIL_BOOTS) return 0.04;

        return 0.0;
    }
}
