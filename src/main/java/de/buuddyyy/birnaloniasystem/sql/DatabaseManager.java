package de.buuddyyy.birnaloniasystem.sql;

import de.buuddyyy.birnaloniasystem.BirnaloniaSystemPlugin;
import de.buuddyyy.birnaloniasystem.config.MainConfig;
import de.buuddyyy.birnaloniasystem.sql.entities.HomeEntity;
import de.buuddyyy.birnaloniasystem.sql.entities.PlayerEntity;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
        if (sessionFactory == null || sessionFactory.isOpen()) {
            return;
        }

        final String driverClass = mainConfig.getString("sql.driverClass");
        final String url = mainConfig.getString("sql.url");
        final String username = mainConfig.getString("sql.username");
        final String password = mainConfig.getString("sql.password");
        final String dialect = mainConfig.getString("sql.dialect");
        final boolean showSql = mainConfig.getBoolean("sql.showSql");

        final Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.driver_class", driverClass);
        configuration.setProperty("hibernate.connection.url", url);
        configuration.setProperty("hibernate.connection.username", username);
        configuration.setProperty("hibernate.connection.password", password);
        configuration.setProperty("hibernate.dialect", dialect);
        configuration.setProperty("hibernate.show_sql", String.valueOf(showSql));
        configuration.setProperty("hibernate.format_sql", "true");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");

        configuration.addAnnotatedClass(PlayerEntity.class);
        configuration.addAnnotatedClass(HomeEntity.class);

        final ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties()).build();

        this.sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        this.session = this.sessionFactory.openSession();
        this.criteriaBuilder = this.session.getCriteriaBuilder();
    }

    public <T> T queryResult(Class<T> clazz, final String sql, final Map<String, Object> parameters) {
        return new ArrayList<>(queryResults(clazz, sql, parameters)).get(0);
    }

    public <T> Collection<T> queryResults(Class<T> clazz, final String sql, final Map<String, Object> parameters) {
        Query query = this.session.createNativeQuery(sql, clazz);
        parameters.forEach(query::setParameter);
        return query.getResultList();
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
