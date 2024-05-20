package de.buuddyyy.birnaloniasystem;

import de.buuddyyy.birnaloniasystem.commands.*;
import de.buuddyyy.birnaloniasystem.config.MainConfig;
import de.buuddyyy.birnaloniasystem.events.*;
import de.buuddyyy.birnaloniasystem.handlers.ChestLockHandler;
import de.buuddyyy.birnaloniasystem.handlers.PlayerHandler;
import de.buuddyyy.birnaloniasystem.managers.HomeManager;
import de.buuddyyy.birnaloniasystem.sql.DatabaseManager;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class BirnaloniaSystemPlugin extends JavaPlugin {

    @Getter private static BirnaloniaSystemPlugin plugin;

    private String prefix;

    private MainConfig mainConfig;
    private DatabaseManager databaseManager;
    private PlayerHandler playerHandler;
    private ChestLockHandler chestLockHandler;
    private HomeManager homeManager;

    @Override
    public void onEnable() {
        BirnaloniaSystemPlugin.plugin = this;
        this.mainConfig = new MainConfig(this);
        this.mainConfig.loadConfig();
        this.databaseManager = new DatabaseManager(this, mainConfig);
        this.databaseManager.openConnection();

        this.playerHandler = new PlayerHandler(this);
        this.chestLockHandler = new ChestLockHandler(this);
        this.homeManager = new HomeManager(this, this.databaseManager);

        final PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new BlockBreakListener(this), this);
        pm.registerEvents(new BlockPlaceListener(this), this);
        pm.registerEvents(new FoodLevelChangeListener(this), this);
        pm.registerEvents(new InventoryOpenListener(this), this);
        pm.registerEvents(new PlayerInteractListener(this), this);
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerQuitListener(this), this);
        this.getCommand("chestlock").setExecutor(new ChestLockCommand(this));
        this.getCommand("enderchest").setExecutor(new EnderChestCommand());
        this.getCommand("home").setExecutor(new HomeCommand(this));
        this.getCommand("sethome").setExecutor(new SetHomeCommand(this));
        this.getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
        this.getCommand("spawn").setExecutor(new SpawnCommand(this));
    }

    @Override
    public void onDisable() {
        try {
            this.databaseManager.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setPrefix(String prefix) {
        this.prefix = ChatColor.translateAlternateColorCodes('&', prefix);
    }

}
