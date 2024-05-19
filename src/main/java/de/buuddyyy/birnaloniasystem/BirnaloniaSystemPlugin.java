package de.buuddyyy.birnaloniasystem;

import de.buuddyyy.birnaloniasystem.commands.*;
import de.buuddyyy.birnaloniasystem.config.MainConfig;
import de.buuddyyy.birnaloniasystem.events.PlayerJoinListener;
import de.buuddyyy.birnaloniasystem.sql.DatabaseManager;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class BirnaloniaSystemPlugin extends JavaPlugin {

    @Getter private static BirnaloniaSystemPlugin plugin;

    private String prefix;

    private MainConfig mainConfig;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        BirnaloniaSystemPlugin.plugin = this;
        this.mainConfig = new MainConfig(this);
        this.mainConfig.loadConfig();
        this.databaseManager = new DatabaseManager(this, mainConfig);
        this.databaseManager.openConnection();
        final PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new PlayerJoinListener(this), this);
        Objects.requireNonNull(this.getCommand("enderchest")).setExecutor(new EnderChestCommand());
        Objects.requireNonNull(this.getCommand("home")).setExecutor(new HomeCommand(this));
        Objects.requireNonNull(this.getCommand("sethome")).setExecutor(new SetHomeCommand(this));
        Objects.requireNonNull(this.getCommand("setspawn")).setExecutor(new SetSpawnCommand(this));
        Objects.requireNonNull(this.getCommand("spawn")).setExecutor(new SpawnCommand(this));
    }

    @Override
    public void onDisable() {
        try {
            this.databaseManager.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = ChatColor.translateAlternateColorCodes('&', prefix);
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

}
