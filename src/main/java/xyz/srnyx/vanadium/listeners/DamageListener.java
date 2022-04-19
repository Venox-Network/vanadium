package xyz.srnyx.vanadium.listeners;

import dev.lone.itemsadder.api.CustomStack;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.managers.MessageManager;

import java.util.Random;


public class DamageListener implements Listener {

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim && event.getDamager() instanceof LivingEntity attacker) {
            @Nullable CustomStack main = CustomStack.byItemStack(victim.getInventory().getItemInMainHand());
            @Nullable CustomStack off = CustomStack.byItemStack(victim.getInventory().getItemInOffHand());
            if (((main != null && main.getId().equals("chris_shield")) || (off != null && off.getId().equals("chris_shield"))) && victim.isBlocking()) {
                if (new Random().nextInt(5) == 0) {
                    // Apply potion effect to attacker
                    attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Main.config.getInt("custom-items.chris_shield.block.duration") * 20, 0));

                    // Decrement durability
                    if (main != null) main.setDurability(main.getDurability() - Main.config.getInt("custom-items.chris_shield.block.durability"));
                    if (off != null) off.setDurability(off.getDurability() - Main.config.getInt("custom-items.chris_shield.block.durability"));

                    // Send messages
                    victim.sendMessage(ChatColor.GREEN + "BLOCKED!");
                    if (attacker instanceof Player attackerPlayer) {
                        // Message sent to victim
                        new MessageManager("custom-items.chris_shield.block.victim")
                                .replace("%attacker%", attackerPlayer.getPlayerListName())
                                .send(victim);
                        // Message sent to attacker
                        new MessageManager("custom-items.chris_shield.block.attacker").send(attackerPlayer);
                    }

                    // Display particles
                    attacker.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, attacker.getLocation().add(0, 1.5, 0), 1, 0, 2, 0, 0);
                } else {
                    // Decrement durability
                    if (main != null) main.setDurability(main.getDurability() - 1);
                    if (off != null) off.setDurability(off.getDurability() - 1);
                }
            }
        }
    }
}


/* COOLDOWN CODE
private final Map<UUID, Long> cooldown = new HashMap<>();

boolean notOnCooldown = true;
if (cooldown.containsKey(victim.getUniqueId())) notOnCooldown = (cooldown.get(victim.getUniqueId()) - System.currentTimeMillis()) <= 0;
if (notOnCooldown) {
    cooldown.remove(victim.getUniqueId());
    cooldown.put(victim.getUniqueId(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Main.config.getInt("custom-items.chris_shield.block.cooldown")));
}
*/