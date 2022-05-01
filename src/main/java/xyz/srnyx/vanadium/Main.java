package xyz.srnyx.vanadium;

import github.scarsz.discordsrv.DiscordSRV;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;

import xyz.srnyx.vanadium.commands.*;
import xyz.srnyx.vanadium.commands.tab.*;
import xyz.srnyx.vanadium.listeners.*;
import xyz.srnyx.vanadium.managers.*;


public class Main extends JavaPlugin {
    public static Main plugin;

    // Config files
    public static YamlConfiguration config;
    public static YamlConfiguration messages;
    public static YamlConfiguration lists;

    // Data files
    public static YamlConfiguration locked;
    public static YamlConfiguration trusted;
    public static YamlConfiguration slots;

    /**
     * Everything done when the plugin is enabling
     */
    @Override
    public void onEnable() {
        plugin = this;

        // Create config files
        new ConfigManager("config.yml", false).create();
        new ConfigManager("lists.yml", false).create();
        new ConfigManager("messages.yml", false).create();

        // Create data files
        new ConfigManager("locked.yml", true).create();
        new ConfigManager("trusted.yml", true).create();
        new ConfigManager("slots.yml", true).create();

        // Other stuff
        loadFiles();
        registerRecipes();
        new LockManager().check();
        new SlotManager("locks").check();
        new SlotManager("trusts").check();

        // Startup messages
        getLogger().info(ChatColor.DARK_AQUA + "=======================");
        getLogger().info(ChatColor.AQUA + "    Vanadium Plugin");
        getLogger().info(ChatColor.AQUA + "" + ChatColor.ITALIC + "With code from GeekSMP");
        getLogger().info(ChatColor.DARK_AQUA + "=======================");

        // Register listeners
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new InteractListener(), this);
        getServer().getPluginManager().registerEvents(new JoinLeaveListener(), this);
        getServer().getPluginManager().registerEvents(new MoveListener(), this);
        getServer().getPluginManager().registerEvents(new ItemsAdderListener(), this);

        // Register commands
        registerCommand("bypass", new CommandBypass(), new TabEmpty());
        registerCommand("locktool", new CommandLockTool(), null);
        registerCommand("trust", new CommandTrust(), null);
        registerCommand("trustlist", new CommandTrustList(), null);
        registerCommand("untrust", new CommandUntrust(), null);
        registerCommand("vreload", new CommandReload(), new TabEmpty());
        registerCommand("slot", new CommandSlot(), null);

        // Register plugin-specific stuff
        // DiscordSRV
        if (Bukkit.getPluginManager().getPlugin("DiscordSRV") != null) {
            registerCommand("link", new CommandLink(), new TabEmpty());
            DiscordSRV.api.subscribe(new DiscordListener());
        }
        // PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderManager().register();
            getLogger().info(ChatColor.GREEN + "PlaceholderAPI expansion successfully registered!");
        }
    }

    /**
     * Load config and data files
     */
    public static void loadFiles() {
        // Load config files
        Main.config = new ConfigManager("config.yml", false).load();
        Main.lists = new ConfigManager("lists.yml", false).load();
        Main.messages = new ConfigManager("messages.yml", false).load();

        // Load data files
        Main.locked = new ConfigManager("locked.yml", true).load();
        Main.trusted = new ConfigManager("trusted.yml", true).load();
        Main.slots = new ConfigManager("slots.yml", true).load();
    }

    /**
     * Register a command to the server
     *
     * @param   name        The name of the plugin
     * @param   executor    The command executor
     * @param   completer   The tab completer ({@code null} = online players)
     */
    private void registerCommand(String name, CommandExecutor executor, TabCompleter completer) {
        PluginCommand command = Bukkit.getPluginCommand(name);
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(completer);
        } else {
            getLogger().warning("Could not register command \"" + name + "\"");
        }
    }

    /**
     * Register the recipes created by the plugin
     */
    private void registerRecipes() {
        // Lock Tool
        if (config.getBoolean("lock-tool.recipe") && !config.getBoolean("lock-tool.custom")) {
            ShapedRecipe locktool = new ShapedRecipe(NamespacedKey.minecraft("lock_tool"), LockManager.getLockTool());
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
}
