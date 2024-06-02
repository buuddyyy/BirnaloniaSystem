package de.buuddyyy.birnaloniasystem.handlers;

import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import de.buuddyyy.birnaloniasystem.managers.PlayerManager;
import de.buuddyyy.birnaloniasystem.sql.entities.PlayerEntity;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.Instant;

public class PlayerHandler {

    private final BirnaloniaSystemPlugin plugin;

    @Getter
    private final PlayerManager playerManager;

    public PlayerHandler(BirnaloniaSystemPlugin plugin) {
        this.plugin = plugin;
        this.playerManager = new PlayerManager(plugin, plugin.getDatabaseManager());
    }

    public void handleJoin(Player player) {
        if (!player.hasPlayedBefore()) {
            this.plugin.getTeleportHandler().teleportToSpawn(player);
        }
        if (!playerManager.playerExists(player)) {
            playerManager.createPlayerEntity(player);
            return;
        }
        final PlayerEntity pe = playerManager.getPlayerEntity(player);
        this.plugin.getTeleportHandler().getHomeNames(player);
        if (player.getName().equals(pe.getPlayerName())) {
            return;
        }
        pe.setPlayerName(player.getName());
        this.playerManager.updatePlayerEntity(pe);
    }

    public void handleQuit(Player player) {
        if (!playerManager.playerExists(player))
            return;
        final PlayerEntity pe = this.getPlayerManager().getPlayerEntity(player);
        pe.setLastOnline(Timestamp.from(Instant.now()));
        this.playerManager.updatePlayerEntity(pe);
    }

    public void setAutoChestLock(Player player, boolean value) {
        if (!playerManager.playerExists(player))
            return;
        final PlayerEntity pe = this.getPlayerManager().getPlayerEntity(player);
        pe.setChestLockActive(value);
        this.playerManager.updatePlayerEntity(pe);
    }

    public boolean isAutoChestLockEnabled(Player player) {
        if (!playerManager.playerExists(player))
            return false;
        return this.getPlayerManager().getPlayerEntity(player)
                .isChestLockActive();
    }

}
