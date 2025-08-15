package org.titilda.music.base.database;

import org.titilda.music.base.config.ConfigManager;

import java.sql.*;

public class DatabaseManager {

    private static Connection c = null;

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

    public String getPasswordHash(String username) {
        String passwordHash = null;
        String sql = "SELECT password_hash FROM Users WHERE username = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    passwordHash = rs.getString("password_hash");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving password hash for user " + username + ": " + e.getMessage());
            throw new RuntimeException("Failed to retrieve password hash", e);
        }
        
        return passwordHash;
    }

}
