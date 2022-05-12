package xyz.srnyx.vanadium.managers;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import xyz.srnyx.vanadium.Main;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class DataManager {
    public static final Map<Block, UUID[]> locked = new HashMap<>();
    public static final Map<Block, Material> lockedType = new HashMap<>();
    public static final Map<UUID, List<UUID>> trusted = new HashMap<>();
    public static final Map<UUID, int[]> slots = new HashMap<>();

    /**
     * Converts data on file to data in maps on plugin enable
     */
    public void onEnable() {
        // locked
        YamlConfiguration dataLocked = new FileManager("locked.yml", true).load();
        for (String key : dataLocked.getKeys(false)) {
            String[] args = key.split("=");
            World world = Bukkit.getWorld(args[0]);
            int x = Integer.parseInt(args[1]);
            int y = Integer.parseInt(args[2]);
            int z = Integer.parseInt(args[3]);

            Block block = world != null ? world.getBlockAt(x, y, z) : null;
            UUID placer = null;
            UUID locker = null;
            Material type = Material.valueOf(args[4]);

            String placerString = dataLocked.getString(key + ".placer");
            if (placerString != null) placer = UUID.fromString(placerString);

            String lockerString = dataLocked.getString(key + ".locker");
            if (lockerString != null) locker = UUID.fromString(lockerString);

            locked.put(block, new UUID[]{placer, locker});
            lockedType.put(block, type);
        }


        // trusted
        YamlConfiguration dataTrusted = new FileManager("trusted.yml", true).load();
        for (String key : dataTrusted.getKeys(false)) {
            List<UUID> trustedPlayers = new ArrayList<>();
            for (String key2 : dataTrusted.getStringList(key)) trustedPlayers.add(UUID.fromString(key2));
            trusted.put(UUID.fromString(key), trustedPlayers);
        }


        // slots
        YamlConfiguration dataSlots = new FileManager("slots.yml", true).load();
        for (String key : dataSlots.getKeys(false)) {
            UUID player = UUID.fromString(key);
            int locks = dataSlots.getInt(key + ".locks");
            int trusts = dataSlots.getInt(key + ".trusts");
            slots.put(player, new int[]{locks, trusts});
        }


        // Start auto-save
        if (Objects.equals(Main.config.getString("auto-save.enabled"), "true")) {
            long interval = Main.config.getInt("auto-save.interval") * 20L;
            new BukkitRunnable() {
                public void run() {
                    save();
                }
            }.runTaskTimer(Main.plugin, interval, interval);
        }
    }

    /**
     * Saves data in maps to files
     */
    public void save() {
        // locked.yml
        YamlConfiguration lockedYaml = new YamlConfiguration();
        for (Block block : locked.keySet()) {
            String id = block.getWorld().getName() + "=" + block.getX() + "=" + block.getY() + "=" + block.getZ() + "=" + DataManager.lockedType.get(block);

            String placerString = null;
            UUID placer = new PlaceManager(block).getPlacer();
            if (placer != null) placerString = placer.toString();

            String lockerString = null;
            UUID locker = new LockManager(block, null).getLocker();
            if (locker != null) lockerString = locker.toString();

            lockedYaml.set(id + ".placer", placerString);
            lockedYaml.set(id + ".locker", lockerString);
        }
        // Save to file
        try {
            lockedYaml.save(new File(new File(Main.plugin.getDataFolder(), "data"), "locked.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        // trusted.yml
        YamlConfiguration trustedYaml = new YamlConfiguration();
        for (Map.Entry<UUID, List<UUID>> map : trusted.entrySet()) {
            List<String> trustedPlayers = new ArrayList<>();
            for (UUID trustedPlayer : map.getValue()) trustedPlayers.add(trustedPlayer.toString());

            trustedYaml.set(map.getKey().toString(), trustedPlayers);
        }
        // Save to file
        try {
            trustedYaml.save(new File(new File(Main.plugin.getDataFolder(), "data"), "trusted.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        // slots.yml
        final YamlConfiguration slotsYaml = new YamlConfiguration();
        for (UUID id : slots.keySet()) {
            String idString = id.toString();
            int locks = new SlotManager("locks", Bukkit.getOfflinePlayer(id)).getCount();
            int trusts = new SlotManager("trusts", Bukkit.getOfflinePlayer(id)).getCount();

            slotsYaml.set(idString + ".locks", locks);
            slotsYaml.set(idString + ".trusts", trusts);
        }
        // Save to file
        try {
            slotsYaml.save(new File(new File(Main.plugin.getDataFolder(), "data"), "slots.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
