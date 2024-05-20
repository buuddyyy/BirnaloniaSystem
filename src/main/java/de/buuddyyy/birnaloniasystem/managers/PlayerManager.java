package de.buuddyyy.birnaloniasystem.managers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import de.buuddyyy.birnaloniasystem.sql.DatabaseManager;
import de.buuddyyy.birnaloniasystem.sql.entities.ChestLockEntity;
import de.buuddyyy.birnaloniasystem.sql.entities.PlayerEntity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PlayerManager {

    public static final String TABLE_NAME = "players";

    private final LoadingCache<UUID, PlayerEntity> playerEntities;
    private final BirnaloniaSystemPlugin plugin;
    private final DatabaseManager databaseManager;

    public PlayerManager(BirnaloniaSystemPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.playerEntities = CacheBuilder.newBuilder().build(new CacheLoader<UUID, PlayerEntity>() {
            @Override
            public PlayerEntity load(UUID uuid) {
                return loadPlayer(uuid);
            }
        });
    }

    public boolean playerExists(Player player) {
        return loadPlayer(player.getUniqueId()) != null;
    }

    public PlayerEntity getPlayerEntity(Player player) {
        return getPlayerEntity(player.getUniqueId());
    }

    public PlayerEntity getPlayerEntity(UUID playerUuid) {
        try {
            return playerEntities.get(playerUuid);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void createPlayerEntity(Player player) {
        this.databaseManager.insertEntity(new PlayerEntity(player.getUniqueId(), player.getName()));
    }

    public void updatePlayerEntity(PlayerEntity playerEntity) {
        this.databaseManager.updateEntity(playerEntity);
        this.playerEntities.refresh(playerEntity.getPlayerUuid());
    }

    private PlayerEntity loadPlayer(UUID playerUuid) {
        final String sql = "SELECT * FROM %s WHERE playerUuid=:playerUuid";
        final Map<String, Object> sqlParameter = Maps.newHashMap();
        sqlParameter.put("playerUuid", playerUuid.toString());
        return this.databaseManager.queryResult(PlayerEntity.class,
                String.format(sql, TABLE_NAME), sqlParameter);
    }

    public PlayerEntity getPlayerByName(String playerName) {
        final String sql = "SELECT * FROM %s WHERE LOWER(playerName)=:playerName";
        final Map<String, Object> sqlParameter = Maps.newHashMap();
        sqlParameter.put("playerName", playerName.toLowerCase());
        return this.databaseManager.queryResult(PlayerEntity.class,
                String.format(sql, TABLE_NAME), sqlParameter);
    }

}
