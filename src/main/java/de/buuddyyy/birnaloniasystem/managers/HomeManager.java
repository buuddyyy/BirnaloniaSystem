package de.buuddyyy.birnaloniasystem.managers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import de.buuddyyy.birnaloniasystem.sql.DatabaseManager;
import de.buuddyyy.birnaloniasystem.sql.entities.HomeEntity;
import de.buuddyyy.birnaloniasystem.sql.entities.PlayerEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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
            public HashMap<String, HomeEntity> load(UUID uuid) {
                return loadAllHomes(uuid);
            }
        });
    }

    public HashMap<String, HomeEntity> getHomes(UUID playerUuid) {
        try {
            return playerHomes.get(playerUuid);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private HashMap<String, HomeEntity> loadAllHomes(UUID playerUuid) {
        final HashMap<String, HomeEntity> homes = Maps.newHashMap();
        final String sql = "SELECT * FROM %s WHERE playerId=:playerId";
        final Map<String, Object> parameters = Maps.newHashMap();
        final PlayerEntity pe = plugin.getPlayerHandler().getPlayerManager()
                .getPlayerEntity(playerUuid);
        parameters.put("playerId", pe.getId());
        List<HomeEntity> list = (List<HomeEntity>) databaseManager.queryResults(HomeEntity.class,
                String.format(sql, TABLE_NAME), parameters);
        list.forEach(he -> homes.put(he.getName(), he));
        return homes;
    }
    
}
