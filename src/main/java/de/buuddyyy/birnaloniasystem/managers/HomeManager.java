package de.buuddyyy.birnaloniasystem.managers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import de.buuddyyy.birnaloniasystem.sql.DatabaseManager;
import de.buuddyyy.birnaloniasystem.sql.entities.HomeEntity;

import java.util.HashMap;
import java.util.UUID;

public class HomeManager {

    public static final String TABLE_NAME = "homes";

    private final LoadingCache<UUID, HashMap<String, HomeEntity>> playerHomes;
    private final BirnaloniaSystemPlugin plugin;
    private final DatabaseManager databaseManager;

    public HomeManager(BirnaloniaSystemPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.playerHomes = CacheBuilder.newBuilder().build(new CacheLoader<UUID, HashMap<String, HomeEntity>>() {
            @Override
            public HashMap<String, HomeEntity> load(UUID uuid) throws Exception {
                return loadAllHomes(uuid);
            }
        });
    }

    public HashMap<String, HomeEntity> loadAllHomes(UUID playerUuid) {
        final HashMap<String, HomeEntity> homes = Maps.newHashMap();
        final String sql = "SELECT * FROM %s WHERE playerId=:playerId";
        return homes;
    }
}
