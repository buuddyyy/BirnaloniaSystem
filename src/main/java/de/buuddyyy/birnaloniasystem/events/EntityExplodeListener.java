package de.buuddyyy.birnaloniasystem.events;

import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityExplodeListener implements Listener {

    private final BirnaloniaSystemPlugin plugin;

    public EntityExplodeListener(BirnaloniaSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        this.plugin.getChestLockHandler().handleExplode(event.blockList(), event);
    }

}
