package de.buuddyyy.birnaloniasystem.handlers;

import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import de.buuddyyy.birnaloniasystem.managers.PlayerManager;
import de.buuddyyy.birnaloniasystem.sql.entities.PlayerEntity;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.Instant;

public class PlayerHandler {

    private final BirnaloniaSystemPlugin plugin;
    private final PlayerManager playerManager;

    public PlayerHandler(BirnaloniaSystemPlugin plugin) {
        this.plugin = plugin;
        this.playerManager = new PlayerManager(plugin, plugin.getDatabaseManager());
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public void handleJoin(Player player) {
        if (!playerManager.playerExists(player)) {
            playerManager.createPlayerEntity(player);
            return;
        }
        final PlayerEntity pe = playerManager.getPlayerEntity(player);
        if (!player.getName().equals(pe.getPlayerName())) {
            pe.setPlayerName(player.getName());
            this.playerManager.updatePlayerEntity(pe);
        }
    }

    public void handleQuit(Player player) {
        if (!playerManager.playerExists(player))
            return;
        final PlayerEntity pe = this.getPlayerManager().getPlayerEntity(player);
        pe.setLastOnline(Timestamp.from(Instant.now()));
    }



}
