package org.titilda.music.base.database;

import org.titilda.music.base.config.ConfigManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    // Optional: load the driver explicitly
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found!", e);
        }
    }

    /**
     * Returns a connection to the PostgreSQL database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                ConfigManager.getString(ConfigManager.ConfigKey.DATABASE_URL),
                ConfigManager.getString(ConfigManager.ConfigKey.DATABASE_USER),
                ConfigManager.getString(ConfigManager.ConfigKey.DATABASE_PASSWORD)
        );
    }
}
