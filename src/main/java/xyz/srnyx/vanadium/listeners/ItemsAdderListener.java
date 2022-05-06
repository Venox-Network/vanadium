package xyz.srnyx.vanadium.listeners;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.managers.DamageManager;
import xyz.srnyx.vanadium.managers.ItemsAdderManager;
import xyz.srnyx.vanadium.managers.MessageManager;

import java.util.*;
import java.util.concurrent.TimeUnit;


public class ItemsAdderListener implements Listener {
    private final Map<UUID, ItemStack> axes = new HashMap<>();

    /**
     * Called when a player interacts
     */
    @EventHandler
    public void interact(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemsAdderManager ItemsAdderManager = new ItemsAdderManager(player);
        boolean leftClick = (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK);
        boolean rightClick = (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK);

        if (player.isSneaking()) {
            if (ItemsAdderManager.notOnCooldown()) {
                // nyx_wand
                if (leftClick && ItemsAdderManager.holdingItem(false, "nyx_wand")) {
                    ItemsAdderManager.cooldown();
                    ItemsAdderManager.durability(false, 2);

                    // TNT item
                    Item tnt = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.TNT));
                    tnt.setVelocity(player.getEyeLocation().getDirection().multiply(1.5)); // Makes the TNT item move forward
                    tnt.setPickupDelay(32767); // Make it so the TNT item can't be picked up
                    tnt.setCustomNameVisible(true);
                    tnt.setInvulnerable(true);
                    tnt.setGlowing(true);
                    // TNT item - Name
                    Map<Item, Long> timer = new HashMap<>();
                    timer.put(tnt, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Main.config.getInt("custom-items.nyx_wand.tnt.fuse")));
                    BukkitRunnable runnable = new BukkitRunnable() {
                        public void run() {
                            tnt.setCustomName(ChatColor.RED + "" + ChatColor.BOLD + (TimeUnit.MILLISECONDS.toSeconds(timer.get(tnt) - System.currentTimeMillis()) + 1));
                            tnt.getWorld().spawnParticle(Particle.END_ROD, tnt.getLocation(), 1, 0, 0, 0);
                        }
                    };
                    runnable.runTaskTimer(Main.plugin, 0, 20);

                    // Creeper
                    new BukkitRunnable() {
                        public void run() {
                            timer.remove(tnt);
                            tnt.remove();
                            runnable.cancel();
                            Creeper creeper = (Creeper) tnt.getWorld().spawnEntity(tnt.getLocation(), EntityType.CREEPER);
                            creeper.setCustomName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "SRNYX BOMB");
                            creeper.setExplosionRadius(2);
                            creeper.setCustomNameVisible(false);
                            creeper.setInvisible(true);
                            creeper.setPowered(true);
                            creeper.setSilent(true);
                            creeper.setAI(false);
                            creeper.explode();
                        }
                    }.runTaskLater(Main.plugin, Main.config.getInt("custom-items.nyx_wand.tnt.fuse") * 20L);
                }

                // chris_shield
                if (rightClick && ItemsAdderManager.holdingItem(true, "chris_shield")) {
                    ItemsAdderManager.cooldown();
                    player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation(), 20, 1, 1, 1);
                    ItemsAdderManager.durability(true, 6);

                    // Damage entities
                    //noinspection ConstantConditions
                    for (Entity entity : player.getLocation().getWorld().getEntities()) {
                        if (entity instanceof LivingEntity entityLiving && entityLiving != player && entityLiving.hasLineOfSight(player)) {
                            if (entityLiving.getLocation().distance(player.getLocation()) <= Main.config.getInt("custom-items.chris_shield.damage.range")) {
                                if (entityLiving instanceof Player livingPlayer) {
                                    if (!livingPlayer.isBlocking()) DamageManager.damage(entityLiving, Main.config.getInt("custom-items.chris_shield.damage.amount"));
                                } else DamageManager.damage(entityLiving, Main.config.getInt("custom-items.chris_shield.damage.amount"));

                                entityLiving.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, entityLiving.getLocation().add(0, 1.5, 0), 1, 0, 0, 0);
                            }
                        }
                    }
                }
            }

            // erin_axe
            if (ItemsAdderManager.holdingItem(false, "chris_axe")) {
                // Lightning
                if (leftClick) {
                    player.getWorld().strikeLightning(player.getTargetBlock(null, Main.config.getInt("custom-items.chris_axe.lightning.range")).getLocation());
                    ItemsAdderManager.durability(false, 4);
                }

                // Throw
                if (rightClick) {
                    // Remove axe
                    axes.put(player.getUniqueId(), player.getInventory().getItemInMainHand().clone());
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                    // Spawn arrow
                    Location eye = player.getEyeLocation();
                    Arrow arrow = player.getWorld().spawnArrow(eye, eye.getDirection(), Main.config.getInt("custom-items.chris_axe.throw.speed"), 0);
                    arrow.setDamage(Main.config.getInt("custom-items.chris_axe.throw.damage"));
                    arrow.setCustomName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "AXE");
                    arrow.setColor(Color.fromRGB(80, 80, 80));
                    arrow.setCustomNameVisible(true);
                    arrow.setPersistent(true);
                    arrow.setShooter(player);
                    arrow.setGlowing(true);
                }
            }
        }
    }

    /**
     * Called when an entity is damaged by another entity
     */
    @EventHandler
    public void damageEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim && event.getDamager() instanceof LivingEntity attacker) {
            ItemsAdderManager ItemsAdderManager = new ItemsAdderManager(victim);
            if (victim.isBlocking() && ItemsAdderManager.holdingItem(true, "chris_shield")) {
                if (new Random().nextInt(5) == 0) {
                    attacker.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Main.config.getInt("custom-items.chris_shield.block.duration") * 20, 0));
                    attacker.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, attacker.getLocation().add(0, 1.5, 0), 1, 0, 2, 0, 0);
                    ItemsAdderManager.durability(true, 3);

                    if (attacker instanceof Player attackerPlayer) {
                        // Message sent to victim
                        new MessageManager("custom-items.chris_shield.block.victim")
                                .replace("%attacker%", attackerPlayer.getPlayerListName())
                                .send(victim);
                        // Message sent to attacker
                        new MessageManager("custom-items.chris_shield.block.attacker")
                                .replace("%victim%", victim.getPlayerListName())
                                .send(attackerPlayer);
                    }
                } else {
                    ItemsAdderManager.durability(true, 1);
                }
            }
        }
    }

    /**
     * Called when a projectile hits something
     */
    @EventHandler
    public void projectile(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow arrow && arrow.getShooter() instanceof Player player) {
            if (arrow.getCustomName() != null && arrow.getCustomName().equals(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "AXE")) {
                // Add axe back to inventory
                player.getInventory().addItem(axes.get(player.getUniqueId()));
                axes.remove(player.getUniqueId());

                new ItemsAdderManager(player).durability(false, 2);
                arrow.remove();
            }
        }
    }

    /**
     * Called when an entity explodes
     */
    @EventHandler
    public void explode(EntityExplodeEvent event) {
        boolean name = Objects.equals(event.getEntity().getCustomName(), ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "SRNYX BOMB");
        if (!Main.config.getBoolean("custom-items.nyx_wand.tnt.blocks") && event.getEntity().getType() == EntityType.CREEPER && name) event.blockList().clear();
    }
}
