package network.venox.vanadium.managers;

import network.venox.vanadium.Main;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

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
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void create() {
        if (!configFolder.exists()) configFolder.mkdir();

        if (data) {
            if (!dataFolder.exists()) dataFolder.mkdir();

            try {
                new File(dataFolder, name).createNewFile();
            } catch (IOException e) {
                Main.plugin.getLogger().warning(ChatColor.translateAlternateColorCodes('&', "&cFailed to create &4" + name + " &cfile"));
            }

            return;
        }

        if (new File(configFolder, name).exists()) return;
        Main.plugin.saveResource(name, false);
    }

    /**
     * Loads a file
     *
     * @return  the loaded file
     */
    public YamlConfiguration load() {
        return YamlConfiguration.loadConfiguration(new File(data ? dataFolder : configFolder, name));
    }
}
