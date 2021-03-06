package network.venox.vanadium.listeners;

import com.olliez4.interface4.util.ParticleUtils;

import dev.lone.itemsadder.api.CustomEntity;
import dev.lone.itemsadder.api.CustomStack;

import network.venox.vanadium.Main;
import network.venox.vanadium.managers.DamageManager;
import network.venox.vanadium.managers.ItemsAdderManager;
import network.venox.vanadium.managers.MessageManager;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class ItemsAdderListener implements Listener {
    static final Map<Warden, LivingEntity> entities = new ConcurrentHashMap<>();

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

        // nyx_wand
        if (iam.holdingItem(false, "nyx_wand") && leftClick && sneaking && cooldown) {
            iam.cooldown();
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
            final BukkitRunnable runnable = new BukkitRunnable() {public void run() {
                tnt.setCustomName(ChatColor.RED + "" + ChatColor.BOLD + Math.round((timer.get(tnt) - System.currentTimeMillis()) / 100.0) / 10.0);
            }};
            runnable.runTaskTimer(Main.plugin, 0, 1);

            // Creeper
            new BukkitRunnable() {public void run() {
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
            }}.runTaskLater(Main.plugin, fuse * 20L);
        }

        // chris_shield
        if (iam.holdingItem(true, "chris_shield") && rightClick && sneaking && cooldown) {
            iam.cooldown();
            iam.durability(true, 6);

            player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation(), 20, 1, 1, 1);

            // Damage entities
            final World world = player.getWorld();
            for (final Entity entity : world.getEntities()) {
                if (!(entity instanceof LivingEntity entityLiving) || entityLiving == player || !entityLiving.hasLineOfSight(player)) return;
                if (entityLiving.getLocation().distance(player.getLocation()) > Main.config.getInt("custom-items.chris_shield.damage.range")) return;

                if (entityLiving instanceof Player livingPlayer) {
                    if (livingPlayer.isBlocking()) return;
                    DamageManager.damage(entityLiving, Main.config.getInt("custom-items.chris_shield.damage.amount"));
                } else DamageManager.damage(entityLiving, Main.config.getInt("custom-items.chris_shield.damage.amount"));

                entityLiving.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, entityLiving.getLocation().add(0, 1.5, 0), 1, 0, 0, 0);
            }
        }

        // chris_axe
        if (iam.holdingItem(false, "chris_axe")) {
            // Lightning
            if (leftClick && cooldown) {
                iam.cooldown();
                iam.durability(false, 4);

                player.getWorld().strikeLightning(player.getTargetBlock(null, Main.config.getInt("custom-items.chris_axe.lightning.range")).getLocation());
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
                new BukkitRunnable() {public void run() {
                    if (arrow.isValid()) iam.axe(arrow, slot);
                }}.runTaskLater(Main.plugin, Main.config.getInt("custom-items.chris_axe.throw.return") * 20L);
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
            if (!(player.getInventory().getItemInMainHand().getItemMeta() instanceof CrossbowMeta cb) || !cb.hasChargedProjectiles()) return;

            // Velocity multiplier
            double multiplier = Main.config.getDouble("custom-items.vanadium_crossbow.speed.arrow");
            for (ItemStack list : cb.getChargedProjectiles()) {
                if (list.getItemMeta() instanceof FireworkMeta meta && meta.getPower() != 0) {
                    multiplier = Main.config.getDouble("custom-items.vanadium_crossbow.speed.firework." + meta.getPower());
                }
            }

            // Summon pearl
            final Location loc = player.getEyeLocation().add(player.getLocation().getDirection());
            final EnderPearl pearl = (EnderPearl) player.getWorld().spawnEntity(loc, EntityType.ENDER_PEARL);
            pearl.setVelocity(player.getEyeLocation().getDirection().multiply(multiplier));
            pearl.setPersistent(true);
            pearl.setShooter(player);
        }

        // warden_horn
        if (iam.holdingItem(false, "warden_horn") && rightClick && cooldown) {
            if (!(iam.getTarget(15) instanceof LivingEntity target) || target instanceof Warden) return;
            iam.cooldown();
            final Warden warden = (Warden) player.getWorld().spawnEntity(player.getLocation(), EntityType.WARDEN);
            entities.put(warden, target);

            final BukkitRunnable runnable = new BukkitRunnable() {public void run() {
                // Keep targeted entity as Warden's target
                warden.setAnger(target, 100);

                // Spawn particles over target
                target.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, target.getLocation().add(0, 1.5, 0), 1, 0, 0, 0);

                if (target.isDead() && !warden.isDead()) {
                    warden.remove();
                    entities.remove(warden);
                    warden.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, warden.getLocation().add(0, 0.5, 0), 1);
                    cancel();
                }

                if (warden.isDead()) cancel();
            }};
            runnable.runTaskTimer(Main.plugin, 0, 20);

            // Warden despawn timer
            new BukkitRunnable() {public void run() {
                if (!warden.isDead()) {
                    warden.remove();
                    entities.remove(warden);
                    warden.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, warden.getLocation().add(0, 0.5, 0), 1);
                }
                if (!runnable.isCancelled()) runnable.cancel();
            }}.runTaskLater(Main.plugin, Main.config.getInt("custom-items.warden_horn.despawn") * 20L);
        }

        // skulk_blaster
        if (iam.holdingItem(false, "skulk_blaster") && rightClick && cooldown) {
            if (!(iam.getTarget(15) instanceof LivingEntity target)) return;
            iam.cooldown();

            // Visual/sound
            ParticleUtils.drawLine(player.getLocation().add(0, 1, 0), target.getLocation().add(0, 0.5, 0), 1, Particle.SONIC_BOOM);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_TENDRIL_CLICKS, 1, 1);

            // Damage target
            DamageManager.damage(target, Main.config.getInt("custom-items.skulk_blaster.damage"));
        }
    }

    /**
     * Called when an entity is damaged by another entity
     */
    @EventHandler
    public void damageEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim) || !(event.getDamager() instanceof LivingEntity attacker)) return;


        // warden_horn

        if (attacker instanceof Warden && entities.containsKey(attacker)) {
            if (victim != entities.get(attacker)) event.setCancelled(true);
            attacker.remove();
            entities.remove(attacker);
            attacker.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, attacker.getLocation().add(0, 0.5, 0), 1);
            return;
        }


        // chris_shield

        if (!(event.getEntity() instanceof Player victimPlayer)) return;
        final ItemsAdderManager iam = new ItemsAdderManager(victimPlayer);
        if (!iam.holdingItem(true, "chris_shield") || !victimPlayer.isBlocking()) return;
        if (new Random().nextInt(5) != 0) {
            iam.durability(true, 1);
            return;
        }

        attacker.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Main.config.getInt("custom-items.chris_shield.block.duration") * 20, 0));
        attacker.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, attacker.getLocation().add(0, 1.5, 0), 1, 0, 2, 0, 0);
        iam.durability(true, 3);

        if (!(attacker instanceof Player attackerPlayer)) return;
        // Message sent to victim
        new MessageManager("custom-items.chris_shield.block.victim")
                .replace("%attacker%", attackerPlayer.getPlayerListName())
                .send(victimPlayer);
        // Message sent to attacker
        new MessageManager("custom-items.chris_shield.block.attacker")
                .replace("%victim%", victimPlayer.getPlayerListName())
                .send(attackerPlayer);
    }

    /**
     * Called when an entity shoots something
     */
    @EventHandler
    public void shoot(EntityShootBowEvent event) {
        if (event.getBow() == null || !Main.itemsAdderInstalled()) return;

        final CustomStack custom = CustomStack.byItemStack(event.getBow());
        if (custom != null && custom.getId().equals("vanadium_crossbow")) event.setCancelled(true);
    }

    /**
     * Called when a projectile hits something
     */
    @EventHandler
    public void projectile(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow) || !(arrow.getShooter() instanceof Player player)) return;
        if (arrow.getCustomName() == null || !arrow.getCustomName().equals(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "AXE")) return;

        new ItemsAdderManager(player).axe(arrow, player.getInventory().getHeldItemSlot());
        if (event.getHitEntity() != null) player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, (float) 0.1, 1);
    }

    /**
     * Called when an entity explodes
     */
    @EventHandler
    public void explode(EntityExplodeEvent event) {
        final boolean name = Objects.equals(event.getEntity().getCustomName(), ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "SRNYX BOMB");
        if (!Main.config.getBoolean("custom-items.nyx_wand.tnt.blocks") && event.getEntity().getType() == EntityType.CREEPER && name) event.blockList().clear();
    }
}
