package xyz.srnyx.vanadium.listeners;

import dev.lone.itemsadder.api.CustomStack;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

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
import xyz.srnyx.vanadium.managers.MessageManager;

import javax.annotation.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ItemsAdderListener implements Listener {
    private final Map<UUID, Long> cooldown = new HashMap<>();

    private final Map<UUID, ItemStack> axes = new HashMap<>();

    public boolean notOnCooldown(Player player) {
        if (cooldown.containsKey(player.getUniqueId())) return (cooldown.get(player.getUniqueId()) - System.currentTimeMillis()) <= 0;
        return true;
    }

    public boolean holdingItem(Player player, String item, boolean offhand) {
        @Nullable CustomStack mainCustom = CustomStack.byItemStack(player.getInventory().getItemInMainHand());
        if (offhand) {
            @Nullable CustomStack offCustom = CustomStack.byItemStack(player.getInventory().getItemInOffHand());
            return (mainCustom != null && mainCustom.getId().equals(item)) || (offCustom != null && offCustom.getId().equals(item));
        }
        return mainCustom != null && mainCustom.getId().equals(item);
    }

    public void durability(Player player, int durability, boolean offhand) {
        @Nullable CustomStack mainCustom = CustomStack.byItemStack(player.getInventory().getItemInMainHand());
        if (mainCustom != null) mainCustom.setDurability(mainCustom.getDurability() - durability);
        if (offhand) {
            @Nullable CustomStack offCustom = CustomStack.byItemStack(player.getInventory().getItemInOffHand());
            if (offCustom != null) offCustom.setDurability(offCustom.getDurability() - durability);
        }
    }

    @EventHandler
    public void interact(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        boolean leftClick = (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK);
        boolean rightClick = (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK);
        boolean sneaking = player.isSneaking();
        boolean notOnCooldown = notOnCooldown(player);

        if ((leftClick || rightClick) && sneaking && notOnCooldown) {
            // Display action bar
            new BukkitRunnable() {
                public void run() {
                    if (notOnCooldown(player)) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                        cancel();
                    } else {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                                new MessageManager("custom-items.action-bar")
                                        .replace("%seconds%", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(cooldown.get(player.getUniqueId()) - System.currentTimeMillis()) + 1))
                                        .string()));
                    }
                }
            }.runTaskTimer(Main.plugin, 0, 20);

            // Cooldown stuff
            cooldown.remove(player.getUniqueId());
            cooldown.put(player.getUniqueId(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Main.config.getInt("custom-items.cooldown")));

            // nyx_wand
            if (leftClick && holdingItem(player, "nyx_wand", false)) {
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

                // Decrement durability
                durability(player, 2, false);
            }

            // chris_shield
            if (rightClick && holdingItem(player, "chris_shield", true)) {
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

                // Display blocker particle
                player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation(), 20, 1, 1, 1);

                // Decrement durability
                durability(player, 6, true);
            }

            // erin_axe
            if (holdingItem(player, "erin_axe", false)) {
                // Lightning
                if (leftClick) {
                    // Summon lightning
                    player.getWorld().strikeLightning(player.getTargetBlock(null, Main.config.getInt("custom-items.erin_axe.lightning.range")).getLocation());

                    // Decrement durability
                    durability(player, 4, false);
                }

                // Throw
                if (rightClick) {
                    // Remove axe
                    axes.put(player.getUniqueId(), player.getInventory().getItemInMainHand().clone());
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                    // Spawn arrow
                    Location eye = player.getEyeLocation();
                    Arrow arrow = player.getWorld().spawnArrow(eye, eye.getDirection(), Main.config.getInt("custom-items.erin_axe.throw.speed"), 0);
                    arrow.setDamage(Main.config.getInt("custom-items.erin_axe.throw.damage"));
                    arrow.setCustomName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "AXE");
                    arrow.setColor(Color.fromRGB(80, 80, 80));
                    arrow.setCustomNameVisible(true);
                    arrow.setShooter(player);
                    arrow.setGlowing(true);
                }
            }
        }
    }

    @EventHandler
    public void damageEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim && event.getDamager() instanceof LivingEntity attacker) {
            @Nullable CustomStack main = CustomStack.byItemStack(victim.getInventory().getItemInMainHand());
            @Nullable CustomStack off = CustomStack.byItemStack(victim.getInventory().getItemInOffHand());
            if (((main != null && main.getId().equals("chris_shield")) || (off != null && off.getId().equals("chris_shield"))) && victim.isBlocking()) {
                if (new Random().nextInt(5) == 0) {
                    // Apply potion effect to attacker
                    attacker.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Main.config.getInt("custom-items.chris_shield.block.duration") * 20, 0));

                    // Decrement durability
                    if (main != null) main.setDurability(main.getDurability() - 3);
                    if (off != null) off.setDurability(off.getDurability() - 3);

                    // Send messages
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

    @EventHandler
    public void projectile(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow arrow && arrow.getShooter() instanceof Player player) {
            if (arrow.getCustomName() != null && arrow.getCustomName().equals(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "AXE")) {
                // Add axe back to inventory
                player.getInventory().addItem(axes.get(player.getUniqueId()));
                axes.remove(player.getUniqueId());

                // Decrement durability
                durability(player, 2, false);

                arrow.remove();
            }
        }
    }

    @EventHandler
    public void explode(EntityExplodeEvent event) {
        boolean name = Objects.equals(event.getEntity().getCustomName(), ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "SRNYX BOMB");
        if (!Main.config.getBoolean("custom-items.nyx_wand.tnt.blocks") && event.getEntity().getType() == EntityType.CREEPER && name) event.blockList().clear();
    }
}
