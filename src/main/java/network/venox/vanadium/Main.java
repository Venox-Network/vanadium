package network.venox.vanadium;

import github.scarsz.discordsrv.DiscordSRV;

import network.venox.vanadium.commands.*;
import network.venox.vanadium.commands.tab.*;
import network.venox.vanadium.listeners.*;
import network.venox.vanadium.managers.DataManager;
import network.venox.vanadium.managers.FileManager;
import network.venox.vanadium.managers.LockManager;
import network.venox.vanadium.managers.PlaceholderManager;
import network.venox.vanadium.managers.slots.SlotManager;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {
    public static Main plugin;
    public static YamlConfiguration config;
    public static YamlConfiguration messages;
    public static YamlConfiguration lists;

    /**
     * Everything done when the plugin is enabling
     */
    @Override
    public void onEnable() {
        plugin = this;

        // Create config files
        new FileManager("config.yml", false).create();
        new FileManager("lists.yml", false).create();
        new FileManager("messages.yml", false).create();

        // Create data files
        new FileManager("locked.yml", true).create();
        new FileManager("trusted.yml", true).create();
        new FileManager("slots.yml", true).create();

        // Other stuff
        loadFiles();
        new DataManager().load();
        new SlotManager().runCheck();

        // Check locked block types
        for (final Block key : DataManager.locked.keySet()) {
            if (!key.getType().equals(DataManager.lockedType.get(key))) {
                DataManager.locked.remove(key);
                DataManager.lockedType.remove(key);
            }
        }

        // Register Lock Tool recipe
        if (config.getBoolean("lock-tool.recipe") && !config.getBoolean("lock-tool.custom")) {
            final ShapedRecipe locktool = new ShapedRecipe(NamespacedKey.minecraft("lock_tool"), LockManager.getLockTool());
            locktool
                    .shape(
                            "  A",
                            " S ",
                            "S  ")
                    .setIngredient('S', Material.STICK)
                    .setIngredient('A', Material.AMETHYST_SHARD);

            // Add recipes
            Bukkit.getServer().addRecipe(locktool);
        }

        // Startup messages
        getLogger().info(ChatColor.DARK_AQUA + "=================");
        getLogger().info(ChatColor.AQUA + " Vanadium Plugin");
        getLogger().info(ChatColor.DARK_AQUA + "=================");

        // Register commands
        registerCommand("bypass", new CommandBypass(), new TabEmpty());
        registerCommand("locktool", new CommandLockTool(), null);
        registerCommand("vreload", new CommandReload(), new TabEmpty());
        registerCommand("slot", new CommandSlot(), null);
        registerCommand("trust", new CommandTrust(), null);
        registerCommand("trustlist", new CommandTrustList(), null);
        registerCommand("untrust", new CommandUntrust(), null);

        final PluginManager pm = Bukkit.getPluginManager();
        // Register listeners
        pm.registerEvents(new BlockListener(), this);
        pm.registerEvents(new InteractListener(), this);
        pm.registerEvents(new JoinLeaveListener(), this);
        pm.registerEvents(new MoveListener(), this);

        // Register plugin-specific stuff
        if (pm.getPlugin("DiscordSRV") != null) DiscordSRV.api.subscribe(new DiscordListener());
        if (pm.getPlugin("PlaceholderAPI") != null) new PlaceholderManager().register();
        if (pm.getPlugin("ItemsAdder") != null) pm.registerEvents(new ItemsAdderListener(), this);
    }

    /**
     * Everything done when the plugin is disabling
     */
    @Override
    public void onDisable() {
        new DataManager().save();
    }

    /**
     * Load config files<br>
     * Data files are loaded via {@link DataManager}
     */
    public static void loadFiles() {
        Main.config = new FileManager("config.yml", false).load();
        Main.lists = new FileManager("lists.yml", false).load();
        Main.messages = new FileManager("messages.yml", false).load();
    }

    /**
     * Register a command to the server
     *
     * @param   name        The name of the command
     * @param   executor    The command executor
     * @param   completer   The tab completer ({@code null} = online players)
     */
    private void registerCommand(String name, CommandExecutor executor, TabCompleter completer) {
        final PluginCommand command = Bukkit.getPluginCommand(name);
        if (command == null) {
            getLogger().warning("Could not register command " + ChatColor.DARK_RED + name);
            return;
        }

        command.setExecutor(executor);
        command.setTabCompleter(completer);
    }

    /**
     * Checks if ItemsAdder is installed
     *
     * @return  {@code true} if ItemsAdder is installed, {@code false} otherwise
     */
    public static boolean itemsAdderInstalled() {
        return Bukkit.getPluginManager().getPlugin("ItemsAdder") != null;
    }

    /**
     * Used for {@link LockManager} and PlaceManager
     *
     * @return  The top and bottom location of the door
     */
    public static Location[] door(Block block) {
        final Door door = (Door) block.getState().getBlockData();
        Location top = block.getLocation();
        Location bottom = block.getLocation();

        if (door.getHalf() == Bisected.Half.TOP) bottom = block.getLocation().subtract(0, 1, 0);
        if (door.getHalf() == Bisected.Half.BOTTOM) top = block.getLocation().add(0, 1, 0);

        return new Location[]{top, bottom};
    }
}
