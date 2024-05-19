package de.buuddyyy.birnaloniasystem.config;

import lombok.SneakyThrows;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class MainConfig {

    private final JavaPlugin plugin;
    private final File file;
    private final FileConfiguration configuration;

    @SneakyThrows
    public MainConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdir();
        this.file = new File(plugin.getDataFolder(), "config.yml");
        if (!this.file.exists())
            this.file.createNewFile();
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
    }

    @SneakyThrows
    public void save() {
        this.configuration.save(this.file);
    }

}
