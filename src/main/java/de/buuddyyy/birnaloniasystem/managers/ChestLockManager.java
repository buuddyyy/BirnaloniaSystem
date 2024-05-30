package de.buuddyyy.birnaloniasystem.managers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import de.buuddyyy.birnaloniasystem.sql.DatabaseManager;
import de.buuddyyy.birnaloniasystem.sql.entities.ChestLockEntity;
import de.buuddyyy.birnaloniasystem.sql.entities.ChestLockPlayerEntity;
import de.buuddyyy.birnaloniasystem.sql.entities.PlayerEntity;
import org.bukkit.Location;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ChestLockManager {

    public static final String CHESTLOCKS_TABLE_NAME = "chestlocks";
    public static final String CHESTLOCKS_PLAYERS_TABLE_NAME = "chestlocks_players";

    private final LoadingCache<Location, ChestLockEntity> chestLockEntities;
    private final LoadingCache<ChestLockEntity, List<ChestLockPlayerEntity>> trustedPlayerEntities;
    private final BirnaloniaSystemPlugin plugin;
    private final DatabaseManager databaseManager;

    public ChestLockManager(BirnaloniaSystemPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.chestLockEntities = CacheBuilder.newBuilder().build(new CacheLoader<Location, ChestLockEntity>() {
            @Override
            public ChestLockEntity load(Location location) {
                return loadChestLock(location);
            }
        });
        this.trustedPlayerEntities = CacheBuilder.newBuilder().build(new CacheLoader<ChestLockEntity, List<ChestLockPlayerEntity>>() {
            @Override
            public List<ChestLockPlayerEntity> load(ChestLockEntity chestLockEntity) throws Exception {
                return loadChestLockPlayers(chestLockEntity);
            }
        });
    }

    public ChestLockEntity getChestLockEntity(Location location) {
        try {
            return chestLockEntities.get(location);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ChestLockPlayerEntity> getTrustedPlayer(ChestLockEntity chestLockEntity) {
        try {
            return trustedPlayerEntities.get(chestLockEntity);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void createChestLock(ChestLockEntity chestLockEntity) {
        if (chestLockExists(chestLockEntity.getLocation()))
            return;
        this.databaseManager.insertEntity(chestLockEntity);
        this.chestLockEntities.refresh(chestLockEntity.getLocation());
    }

    public void deleteChestLock(ChestLockEntity chestLockEntity) {
        if (!chestLockExists(chestLockEntity.getLocation()))
            return;
        try {
            final List<ChestLockPlayerEntity> trustedPlayers = this.trustedPlayerEntities.get(chestLockEntity);
            for (ChestLockPlayerEntity trustedPlayer : trustedPlayers) {
                this.databaseManager.deleteEntity(trustedPlayer);
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        this.trustedPlayerEntities.invalidate(chestLockEntity);
        this.databaseManager.deleteEntity(chestLockEntity);
        this.chestLockEntities.invalidate(chestLockEntity.getLocation());
    }

    public void createTrustedPlayer(ChestLockPlayerEntity chestLockPlayerEntity) {
        if (trustPlayerExists(chestLockPlayerEntity))
            return;
        this.databaseManager.insertEntity(chestLockPlayerEntity);
        this.trustedPlayerEntities.refresh(chestLockPlayerEntity.getChestLockEntity());
    }

    public void deleteTrustedPlayer(ChestLockPlayerEntity chestLockPlayerEntity) {
        if (!trustPlayerExists(chestLockPlayerEntity))
            return;
        this.databaseManager.deleteEntity(chestLockPlayerEntity);
        this.trustedPlayerEntities.refresh(chestLockPlayerEntity.getChestLockEntity());
    }

    public ChestLockPlayerEntity getChestLockPlayer(PlayerEntity playerEntity, ChestLockEntity chestLockEntity) {
        try {
            final List<ChestLockPlayerEntity> trustedPlayers = this.trustedPlayerEntities.get(chestLockEntity);
            for (ChestLockPlayerEntity trustedPlayer : trustedPlayers) {
                if (trustedPlayer.getPlayerEntity().getPlayerUuid().equals(playerEntity.getPlayerUuid())) {
                    return trustedPlayer;
                }
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean chestLockExists(Location location) {
        return this.chestLockEntities.getIfPresent(location) != null;
    }

    public boolean trustPlayerExists(ChestLockPlayerEntity chestLockPlayerEntity) {
        if (this.trustedPlayerEntities.getIfPresent(chestLockPlayerEntity.getChestLockEntity()) == null)
            return false;
        final List<ChestLockPlayerEntity> trustedPlayers = this.getTrustedPlayer(chestLockPlayerEntity.getChestLockEntity());
        for (ChestLockPlayerEntity trustedPlayer : trustedPlayers) {
            if (trustedPlayer.getPlayerEntity().getPlayerUuid().equals(chestLockPlayerEntity
                    .getPlayerEntity().getPlayerUuid())) {
                return true;
            }
        }
        return false;
    }

    private ChestLockEntity loadChestLock(Location location) {
        final String sql = "SELECT * FROM %s WHERE worldName=:worldName AND " +
                "blockX=:blockX AND blockY=:blockY AND blockZ=:blockZ";
        final Map<String, Object> sqlParameters = Maps.newHashMap();
        sqlParameters.put("worldName", location.getWorld().getName());
        sqlParameters.put("blockX", location.getBlockX());
        sqlParameters.put("blockY", location.getBlockY());
        sqlParameters.put("blockZ", location.getBlockZ());
        return this.databaseManager.queryResult(ChestLockEntity.class,
                String.format(sql, CHESTLOCKS_TABLE_NAME), sqlParameters);
    }

    private List<ChestLockPlayerEntity> loadChestLockPlayers(ChestLockEntity chestLockEntity) {
        final String sql = "SELECT * FROM %s WHERE chestLockId=:chestLockId";
        final Map<String, Object> sqlParameters = Maps.newHashMap();
        sqlParameters.put("chestLockId", chestLockEntity.getId());
        return (List<ChestLockPlayerEntity>) this.databaseManager.queryResults(
                ChestLockPlayerEntity.class, String.format(sql, CHESTLOCKS_PLAYERS_TABLE_NAME),
                sqlParameters);
    }

}
