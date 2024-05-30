package de.buuddyyy.birnaloniasystem.config;

import com.google.common.collect.Maps;
import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;

public final class MainConfig {

    private final BirnaloniaSystemPlugin plugin;
    private final File file;
    @Getter private final FileConfiguration configuration;

    private String tabListHeader, tabListFooter, chatFormat;
    private int teleportCooldown;

    @SneakyThrows
    public MainConfig(BirnaloniaSystemPlugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdir();
        this.file = new File(plugin.getDataFolder(), "config.yml");
        if (!this.file.exists())
            this.file.createNewFile();
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
        this.createDefaults();
    }

    public void createDefaults() {
        final Map<String, Object> defaultsMap = Maps.newHashMap();
        defaultsMap.put("general.prefix", "&7[&4Birnalonia&7] &7");
        defaultsMap.put("general.tablist.header", "&c&lBirnalonia");
        defaultsMap.put("general.tablist.footer", "&7Wir haben spaß!");
        defaultsMap.put("teleport-delay", 3);
        defaultsMap.put("sql.driverClass", "com.mysql.jdbc.Driver");
        defaultsMap.put("sql.url", "jdbc:mysql://localhost:3306/yourdatabase");
        defaultsMap.put("sql.username", "yourusername");
        defaultsMap.put("sql.password", "yourpassword");
        defaultsMap.put("sql.showSql", true);
        defaultsMap.put("sql.formatSql", true);
        this.configuration.addDefaults(defaultsMap);
        this.configuration.options().copyDefaults(true);
        this.save();
    }

    public void loadConfig() {
        this.plugin.setPrefix(getString("general.prefix"));
    }

    public String getString(final String key) {
        if (!this.configuration.contains(key))
            throw new IllegalArgumentException("Configuration key \"" + key + "\" does not exists!");
        return this.configuration.getString(key);
    }

    public Boolean getBoolean(final String key) {
        if (!this.configuration.contains(key))
            throw new IllegalArgumentException("Configuration key \"" + key + "\" does not exists!");
        return this.configuration.getBoolean(key);
    }

    @SneakyThrows
    public void save() {
        this.configuration.save(this.file);
    }

}
