package de.buuddyyy.birnaloniasystem;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class BirnaloniaSystemPlugin extends JavaPlugin {

    @Getter private static BirnaloniaSystemPlugin plugin;

    @Override
    public void onEnable() {
        BirnaloniaSystemPlugin.plugin = this;
    }

    @Override
    public void onDisable() {

    }

}
