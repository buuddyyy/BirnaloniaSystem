package de.buuddyyy.birnaloniasystem.events;

import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final BirnaloniaSystemPlugin plugin;

    public PlayerMoveListener(BirnaloniaSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        plugin.getTeleportHandler().handleMove(event);
    }

}
