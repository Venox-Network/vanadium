package xyz.srnyx.vanadium.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import xyz.srnyx.vanadium.Main;

import java.io.File;
import java.io.IOException;


public class FileManager {
    private final String name;
    private final boolean data;

    private final File configFolder = Main.plugin.getDataFolder();
    private final File dataFolder = new File(configFolder, "data");

    public FileManager(String name, boolean data) {
        this.name = name;
        this.data = data;
    }

    /**
     * Creates a new file
     */
    public void create() {
        if (data) {
            if (!configFolder.exists()) {
                if (configFolder.mkdir()) {
                    Main.plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully created &2config &afolder"));
                } else {
                    Main.plugin.getLogger().warning(ChatColor.translateAlternateColorCodes('&', "&cFailed to create &4config &cfolder"));
                }
            }
            if (!dataFolder.exists()) {
                if (dataFolder.mkdir()) {
                    Main.plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully created &2data &afolder"));
                } else {
                    Main.plugin.getLogger().warning(ChatColor.translateAlternateColorCodes('&', "&cFailed to create &4data &cfolder"));
                }
            }

            try {
                if (new File(dataFolder, name).createNewFile()) {
                    Main.plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully created &2" + name + " &afile"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (!configFolder.exists()) {
                if (configFolder.mkdir()) {
                    Main.plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully created &2config &afolder"));
                } else {
                    Main.plugin.getLogger().warning(ChatColor.translateAlternateColorCodes('&', "&cFailed to create &4config &cfolder"));
                }
            }

            if (new File(configFolder, name).exists()) return;

            Main.plugin.saveResource(name, false);
        }
    }

    /**
     * Loads a file
     *
     * @return Returns the loaded file
     */
    public YamlConfiguration load() {
        File file;
        if (data) {
            file = dataFolder;
        } else {
            file = configFolder;
        }

        return YamlConfiguration.loadConfiguration(new File(file, name));
    }
}
