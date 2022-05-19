package xyz.srnyx.vanadium.listeners;

import dev.lone.itemsadder.api.CustomEntity;
import dev.lone.itemsadder.api.CustomStack;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.managers.DamageManager;
import xyz.srnyx.vanadium.managers.ItemsAdderManager;
import xyz.srnyx.vanadium.managers.MessageManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class ItemsAdderListener implements Listener {
    /**
     * Called when a player interacts
     */
    @EventHandler
    public void interact(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemsAdderManager iam = new ItemsAdderManager(player);
        final boolean sneaking = player.isSneaking();
        final boolean cooldown = iam.notOnCooldown();
        final boolean leftClick = event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;
        final boolean rightClick = event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;

        final long time = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Main.config.getInt("custom-items.cooldown"));

        // nyx_wand
        if (iam.holdingItem(false, "nyx_wand") && leftClick && sneaking && cooldown) {
            iam.cooldown();
            ItemsAdderManager.cooldowns.put(player.getUniqueId(), time);
            iam.durability(false, 2);

            // TNT item
            final Item tnt = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.TNT));
            tnt.setVelocity(player.getEyeLocation().getDirection().multiply(1.5)); // Makes the TNT item move forward
            tnt.setPickupDelay(32767); // Make it so the TNT item can't be picked up
            tnt.setCustomNameVisible(true);
            tnt.setInvulnerable(true);
            tnt.setGlowing(true);

            final int fuse = Main.config.getInt("custom-items.nyx_wand.tnt.fuse");

            // TNT item - Name
            final Map<Item, Long> timer = new ConcurrentHashMap<>();
            timer.put(tnt, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(fuse));
            // Decimal formatting stuff
            final BukkitRunnable runnable = new BukkitRunnable() {
                public void run() {
                    tnt.setCustomName(ChatColor.RED + "" + ChatColor.BOLD + Math.round((timer.get(tnt) - System.currentTimeMillis()) / 100.0) / 10.0);
                }
            };
            runnable.runTaskTimer(Main.plugin, 0, 1);

            // Creeper
            new BukkitRunnable() {
                public void run() {
                    // Remove the TNT item from timer map
                    timer.remove(tnt);
                    // "Kill" the TNT item
                    tnt.remove();
                    // Cancel name changing runnable
                    runnable.cancel();

                    // Summon creeper
                    final Creeper creeper = (Creeper) tnt.getWorld().spawnEntity(tnt.getLocation(), EntityType.CREEPER);
                    creeper.setCustomName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "SRNYX BOMB");
                    creeper.setExplosionRadius(2);
                    creeper.setExplosionRadius(4);
                    creeper.setCustomNameVisible(false);
                    creeper.setInvisible(true);
                    creeper.setSilent(true);
                    creeper.setAI(false);
                    creeper.explode();
                }
            }.runTaskLater(Main.plugin, fuse * 20L);
        }

        // chris_shield
        if (iam.holdingItem(true, "chris_shield") && rightClick && sneaking && cooldown) {
            iam.cooldown();
            ItemsAdderManager.cooldowns.put(player.getUniqueId(), time);
            iam.durability(true, 6);

            player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation(), 20, 1, 1, 1);

            // Damage entities
            final World world = player.getWorld();
            for (final Entity entity : world.getEntities()) {
                if (entity instanceof LivingEntity entityLiving && entityLiving != player && entityLiving.hasLineOfSight(player)) {
                    if (entityLiving.getLocation().distance(player.getLocation()) <= Main.config.getInt("custom-items.chris_shield.damage.range")) {
                        if (entityLiving instanceof Player livingPlayer) {
                            if (!livingPlayer.isBlocking())
                                DamageManager.damage(entityLiving, Main.config.getInt("custom-items.chris_shield.damage.amount"));
                        } else
                            DamageManager.damage(entityLiving, Main.config.getInt("custom-items.chris_shield.damage.amount"));

                        entityLiving.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, entityLiving.getLocation().add(0, 1.5, 0), 1, 0, 0, 0);
                    }
                }
            }
        }

        // chris_axe
        if (iam.holdingItem(false, "chris_axe")) {
            // Lightning
            if (leftClick && cooldown) {
                player.getWorld().strikeLightning(player.getTargetBlock(null, Main.config.getInt("custom-items.chris_axe.lightning.range")).getLocation());
                iam.durability(false, 4);
            }

            // Throw
            if (rightClick) {
                // Remove axe
                ItemsAdderManager.axes.put(player.getUniqueId(), player.getInventory().getItemInMainHand().clone());
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                // Spawn arrow
                final Location eye = player.getEyeLocation();
                final Arrow arrow = player.getWorld().spawnArrow(eye, eye.getDirection(), Main.config.getInt("custom-items.chris_axe.throw.speed"), 0);
                arrow.setDamage(Main.config.getInt("custom-items.chris_axe.throw.damage"));
                arrow.setCustomName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "AXE");
                arrow.setCustomNameVisible(true);
                arrow.setPersistent(true);
                arrow.setShooter(player);
                arrow.setGlowing(true);

                // Remove arrow after a few seconds
                final int slot = player.getInventory().getHeldItemSlot();
                new BukkitRunnable() {
                    public void run() {
                        if (arrow.isValid()) iam.axe(arrow, slot);
                    }
                }.runTaskLater(Main.plugin, Main.config.getInt("custom-items.chris_axe.throw.return") * 20L);
            }
        }

        // nyx_dragon_spawn_egg
        if (iam.holdingItem(false, "nyx_dragon_spawn_egg") && event.getClickedBlock() != null && rightClick) {
            final BlockFace blockFace = event.getBlockFace();
            final Location loc = event.getClickedBlock().getLocation().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
            CustomEntity.spawn("vanadium:nyx_dragon", loc);

            // Remove egg if they are not in creative mode
            if (player.getGameMode() != GameMode.CREATIVE) player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }

        // vanadium_crossbow
        if (iam.holdingItem(true, "vanadium_crossbow") && rightClick && cooldown) {
            if (player.getInventory().getItemInMainHand().getItemMeta() instanceof CrossbowMeta cb && cb.hasChargedProjectiles()) {
                // Summon pearl
                final Location loc = player.getEyeLocation().add(player.getLocation().getDirection());
                final EnderPearl pearl = (EnderPearl) player.getWorld().spawnEntity(loc, EntityType.ENDER_PEARL);
                pearl.setVelocity(player.getEyeLocation().getDirection().multiply(Main.config.getDouble("custom-items.vanadium_crossbow.speed")));
                pearl.setPersistent(true);
                pearl.setShooter(player);
            }
        }
    }

    /**
     * Called when an entity is damaged by another entity
     */
    @EventHandler
    public void damageEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim && event.getDamager() instanceof LivingEntity attacker) {
            final ItemsAdderManager iam = new ItemsAdderManager(victim);
            if (iam.holdingItem(true, "chris_shield") && victim.isBlocking()) {
                if (new Random().nextInt(5) == 0) {
                    attacker.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Main.config.getInt("custom-items.chris_shield.block.duration") * 20, 0));
                    attacker.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, attacker.getLocation().add(0, 1.5, 0), 1, 0, 2, 0, 0);
                    iam.durability(true, 3);

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
                } else iam.durability(true, 1);
            }
        }
    }

    /**
     * Called when an entity shoots something
     */
    @EventHandler
    public void shoot(EntityShootBowEvent event) {
        if (event.getBow() != null && Main.itemsAdderInstalled()) {
            final CustomStack custom = CustomStack.byItemStack(event.getBow());
            if (custom != null && custom.getId().equals("vanadium_crossbow")) {
                event.setCancelled(true);
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
                new ItemsAdderManager(player).axe(arrow, player.getInventory().getHeldItemSlot());

                // Play hit sound
                if (event.getHitEntity() != null) player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, (float) 0.1, 1);
            }
        }
    }

    /**
     * Called when an entity explodes
     */
    @EventHandler
    public void explode(EntityExplodeEvent event) {
        final boolean name = Objects.equals(event.getEntity().getCustomName(), ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "SRNYX BOMB");
        if (!Main.config.getBoolean("custom-items.nyx_wand.tnt.blocks") && event.getEntity().getType() == EntityType.CREEPER && name) {
            event.blockList().clear();
        }
    }
}
