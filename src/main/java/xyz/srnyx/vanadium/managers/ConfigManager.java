package xyz.srnyx.vanadium.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

import xyz.srnyx.vanadium.Main;

import java.io.File;
import java.io.IOException;


@SuppressWarnings({"FieldMayBeFinal", "CanBeFinal"})
public class ConfigManager {
    private String name;
    private boolean data;

    private final File configFolder = Main.plugin.getDataFolder();
    private final File dataFolder = new File(configFolder, "data");

    public ConfigManager(String name, boolean data) {
        this.name = name;
        this.data = data;
    }

    /**
     * Creates a new file
     */
    public void create() {
        if (data) {
            if (!configFolder.exists())
                //noinspection ResultOfMethodCallIgnored
                configFolder.mkdir();
            if (!dataFolder.exists())
                //noinspection ResultOfMethodCallIgnored
                dataFolder.mkdir();
            File file = new File(dataFolder, name);
            if (!file.exists()) {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (!configFolder.exists()) {
                //noinspection ResultOfMethodCallIgnored
                configFolder.mkdir();
            }
            if (new File(configFolder, name).exists()) {
                return;
            }
            Main.plugin.saveResource(name, false);
        }
    }

    /**
     * Saves a data file
     *
     * @param   file    The {@code YamlConfiguration} of the file from main class
     */
    public void saveData(FileConfiguration file) {
        if (data) {
            try {
                file.save(new File(dataFolder, name));
            } catch (IOException e) {
                e.printStackTrace();
            }
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
