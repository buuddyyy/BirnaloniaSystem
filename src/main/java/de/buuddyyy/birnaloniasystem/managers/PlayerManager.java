package de.buuddyyy.birnaloniasystem.managers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import de.buuddyyy.birnaloniasystem.sql.DatabaseManager;
import de.buuddyyy.birnaloniasystem.sql.entities.PlayerEntity;
import org.bukkit.entity.Player;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Map;
import java.util.Objects;
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
            public PlayerEntity load(UUID uuid) throws Exception {
                return loadPlayer(uuid);
            }
        });
    }

    public PlayerEntity getPlayerEntity(Player player) {
        try {
            return playerEntities.get(player.getUniqueId());
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void createPlayerEntity(Player player) {
        final PlayerEntity pe = new PlayerEntity(player.getUniqueId());
        final Session session = this.databaseManager.getSession();
        final Transaction transaction = session.beginTransaction();
        session.persist(pe);
        transaction.commit();
    }

    private PlayerEntity loadPlayer(UUID playerUuid) {
        final String sql = "SELECT * FROM %s WHERE playerUuid=:playerUuid";
        final Map<String, Object> sqlParameter = Maps.newHashMap();
        sqlParameter.put("playerUuid", playerUuid.toString());
        return this.databaseManager.queryResult(PlayerEntity.class, sql, sqlParameter);
    }



}
