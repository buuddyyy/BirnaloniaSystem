package de.buuddyyy.birnaloniasystem.sql;

import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import de.buuddyyy.birnaloniasystem.config.MainConfig;
import de.buuddyyy.birnaloniasystem.sql.converters.UUIDConverter;
import de.buuddyyy.birnaloniasystem.sql.entities.ChestLockEntity;
import de.buuddyyy.birnaloniasystem.sql.entities.ChestLockPlayerEntity;
import de.buuddyyy.birnaloniasystem.sql.entities.HomeEntity;
import de.buuddyyy.birnaloniasystem.sql.entities.PlayerEntity;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public final class DatabaseManager implements AutoCloseable {

    private SessionFactory sessionFactory;
    private Session session;
    private CriteriaBuilder criteriaBuilder;

    private BirnaloniaSystemPlugin plugin;
    private final MainConfig mainConfig;

    public DatabaseManager(BirnaloniaSystemPlugin plugin, MainConfig mainConfig) {
        this.plugin = plugin;
        this.mainConfig = mainConfig;
    }

    public void openConnection() {
        if (sessionFactory != null) {
            return;
        }

        final String driverClass = mainConfig.getString("sql.driverClass");
        final String url = mainConfig.getString("sql.url");
        final String username = mainConfig.getString("sql.username");
        final String password = mainConfig.getString("sql.password");
        final boolean showSql = mainConfig.getBoolean("sql.showSql");
        final boolean formatSql = mainConfig.getBoolean("sql.formatSql");

        final Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.driver_class", driverClass);
        configuration.setProperty("hibernate.connection.url", url);
        configuration.setProperty("hibernate.connection.username", username);
        configuration.setProperty("hibernate.connection.password", password);
        configuration.setProperty("hibernate.show_sql", String.valueOf(showSql));
        configuration.setProperty("hibernate.format_sql", String.valueOf(formatSql));
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");


        configuration.addAnnotatedClass(ChestLockEntity.class);
        configuration.addAnnotatedClass(ChestLockPlayerEntity.class);
        configuration.addAnnotatedClass(HomeEntity.class);
        configuration.addAnnotatedClass(PlayerEntity.class);

        final ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties()).build();

        this.sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        this.session = this.sessionFactory.openSession();
        this.criteriaBuilder = this.session.getCriteriaBuilder();
    }

    public <T> T queryResult(Class<T> clazz, final String sql, final Map<String, Object> parameters) {
        Collection<T> collection = this.queryResults(clazz, sql, parameters);
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        return ((ArrayList<T>) collection).get(0);
    }

    public <T> Collection<T> queryResults(Class<T> clazz, final String sql, final Map<String, Object> parameters) {
        Query query = this.session.createNativeQuery(sql, clazz);
        parameters.forEach(query::setParameter);
        return query.getResultList();
    }

    public void insertEntity(Object entityObject) {
        Transaction transaction = this.session.beginTransaction();
        this.session.persist(entityObject);
        transaction.commit();
    }

    public void updateEntity(Object entityObject) {
        Transaction transaction = this.session.beginTransaction();
        this.session.update(entityObject);
        transaction.commit();
    }

    public void deleteEntity(Object entityObject) {
        Transaction transaction = this.session.beginTransaction();
        this.session.delete(entityObject);
        transaction.commit();
    }

    public Session getSession() {
        return session;
    }

    @Override
    public void close() throws Exception {
        session.close();
        sessionFactory.close();
    }

}
