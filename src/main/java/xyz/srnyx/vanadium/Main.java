package xyz.srnyx.vanadium;

import github.scarsz.discordsrv.DiscordSRV;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ShapedRecipe;

import xyz.srnyx.vanadium.commands.*;
import xyz.srnyx.vanadium.commands.tab.*;
import xyz.srnyx.vanadium.listeners.*;
import xyz.srnyx.vanadium.managers.*;


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
        final PluginManager pm = Bukkit.getPluginManager();

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
        registerRecipes();
        new DataManager().onEnable();
        new LockManager(null, null).check();
        new SlotManager("locks").check();
        new SlotManager("trusts").check();

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

        // Register listeners
        pm.registerEvents(new BlockListener(), this);
        pm.registerEvents(new InteractListener(), this);
        pm.registerEvents(new JoinLeaveListener(), this);
        pm.registerEvents(new MoveListener(), this);
        pm.registerEvents(new ItemsAdderListener(), this);

        // Register plugin-specific stuff
        if (pm.getPlugin("DiscordSRV") != null) DiscordSRV.api.subscribe(new DiscordListener());
        if (pm.getPlugin("PlaceholderAPI") != null) new PlaceholderManager().register();
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
     * @param   name        The name of the plugin
     * @param   executor    The command executor
     * @param   completer   The tab completer ({@code null} = online players)
     */
    private void registerCommand(String name, CommandExecutor executor, TabCompleter completer) {
        final PluginCommand command = Bukkit.getPluginCommand(name);
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(completer);
        } else getLogger().warning("Could not register command " + ChatColor.DARK_RED + name);
    }

    /**
     * Register the recipes created by the plugin
     */
    private void registerRecipes() {
        // Lock Tool
        if (config.getBoolean("lock-tool.recipe") && !config.getBoolean("lock-tool.custom")) {
            final ShapedRecipe locktool = new ShapedRecipe(NamespacedKey.minecraft("lock_tool"), LockManager.getLockTool());
            locktool
                    .shape(
                            "  A",
                            " S ",
                            "S  ")
                    .setIngredient('S', Material.STICK)
                    .setIngredient('A', Material.GOLD_INGOT);

            // Add recipes
            Bukkit.getServer().addRecipe(locktool);
        }
    }

    /**
     * Used for {@link LockManager} and {@link PlaceManager}
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
