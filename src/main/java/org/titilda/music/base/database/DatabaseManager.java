package org.titilda.music.base.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.titilda.music.base.util.ConfigManager;
import static org.titilda.music.base.util.ConfigManager.ConfigKey;

import java.sql.*;
public class DatabaseManager {

    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource ds;


    /*
     * Static block to load the PostgreSQL JDBC driver.
     * This is APPARENTLY necessary to ensure the driver is registered before any connection attempts. (why??) TODO: understand
     */
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found. Include it in your library path.", e);
        }
    }

    static {
        config.setJdbcUrl(ConfigManager.getString(ConfigKey.DATABASE_URL));
        config.setUsername(ConfigManager.getString(ConfigKey.DATABASE_USER));
        config.setPassword(ConfigManager.getString(ConfigKey.DATABASE_PASSWORD));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
    }

    private DatabaseManager() {}

    /**
     * Gets a connection to the postgres database using HikariCP.
     *
     * @return a Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

}
