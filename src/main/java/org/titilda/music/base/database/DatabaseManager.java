package org.titilda.music.base.database;

import org.titilda.music.base.config.ConfigManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    /**
     * Static block to load the PostgreSQL JDBC driver.
     * This is necessary to ensure the driver is registered before any connection attempts.
     */
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found. Include it in your library path.", e);
        }
    }
    /**
     * Returns a connection to the PostgreSQL database.
     */
    public static Connection getConnection() throws SQLException {
        Connection c;
        try {
            c = DriverManager.getConnection(
                    ConfigManager.getString(ConfigManager.ConfigKey.DATABASE_URL),
                    ConfigManager.getString(ConfigManager.ConfigKey.DATABASE_USER),
                    ConfigManager.getString(ConfigManager.ConfigKey.DATABASE_PASSWORD)
            );
        } catch (Exception e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
            throw new SQLException("Failed to connect to the database", e);
        }
        System.out.println("Opened database successfully");
        return c;
    }
}
