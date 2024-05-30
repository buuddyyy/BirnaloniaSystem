package de.buuddyyy.birnaloniasystem.events;

import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;

public class PlayerBedLeaveListener implements Listener {

    private final BirnaloniaSystemPlugin plugin;

    public PlayerBedLeaveListener(BirnaloniaSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        this.plugin.getSkipNightHandler().handleBedLeave(event.getPlayer());
    }

}
