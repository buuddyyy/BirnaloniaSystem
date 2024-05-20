package de.buuddyyy.birnaloniasystem.managers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import de.buuddyyy.birnaloniasystem.sql.DatabaseManager;
import de.buuddyyy.birnaloniasystem.sql.entities.ChestLockEntity;
import lombok.Getter;
import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ChestLockManager {

    public static final String CHESTLOCKS_TABLE_NAME = "chestlocks";
    public static final String CHESTLOCKS_PLAYERS_TABLE_NAME = "chestlocks_players";

    @Getter private final LoadingCache<Location, ChestLockEntity> chestLockEntities;

    private final BirnaloniaSystemPlugin plugin;
    @Getter private final DatabaseManager databaseManager;

    public ChestLockManager(BirnaloniaSystemPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.chestLockEntities = CacheBuilder.newBuilder().build(new CacheLoader<Location, ChestLockEntity>() {
            @Override
            public ChestLockEntity load(Location location) {
                return loadChestLock(location);
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

    public boolean chestLockExists(Location location) {
        return loadChestLock(location) != null;
    }

    public void createChestLock(ChestLockEntity chestLockEntity) {
        if (chestLockExists(chestLockEntity.getLocation()))
            return;
        this.databaseManager.insertEntity(chestLockEntity);
    }

    public void updateChestLock(ChestLockEntity chestLockEntity) {
        this.databaseManager.updateEntity(chestLockEntity);
        this.chestLockEntities.refresh(chestLockEntity.getLocation());
    }

    public void deleteChestLock(ChestLockEntity chestLockEntity) {
        if (!chestLockExists(chestLockEntity.getLocation()))
            return;
        this.databaseManager.deleteEntity(chestLockEntity);
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

}
