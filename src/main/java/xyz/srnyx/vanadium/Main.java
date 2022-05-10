package xyz.srnyx.vanadium;

import github.scarsz.discordsrv.DiscordSRV;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
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
        new LockManager().check();
        new SlotManager("locks").check();
        new SlotManager("trusts").check();

        // Startup messages
        getLogger().info(ChatColor.DARK_AQUA + "=================");
        getLogger().info(ChatColor.AQUA + " Vanadium Plugin");
        getLogger().info(ChatColor.DARK_AQUA + "=================");

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new InteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new JoinLeaveListener(), this);
        Bukkit.getPluginManager().registerEvents(new MoveListener(), this);
        Bukkit.getPluginManager().registerEvents(new ItemsAdderListener(), this);

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
