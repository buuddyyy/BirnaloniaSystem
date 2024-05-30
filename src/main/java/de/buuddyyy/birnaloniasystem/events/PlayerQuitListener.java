package de.buuddyyy.birnaloniasystem.events;

import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final BirnaloniaSystemPlugin plugin;

    public PlayerQuitListener(BirnaloniaSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player p = event.getPlayer();
        plugin.getPlayerHandler().handleQuit(p);
        plugin.getSkipNightHandler().handlePlayerQuit(p);
    }

}
