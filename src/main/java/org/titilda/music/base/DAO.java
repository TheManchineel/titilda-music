package org.titilda.music.base;

import org.titilda.music.base.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DAO {
    private Connection connection;

    public DAO(Connection con) {
        this.connection = con;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Inserts a new user into the database.
     * This method uses a prepared statement to prevent SQL injection.
     * It returns the inserted user object with all fields populated.
     *
     * @param user User object containing the details of the user to be inserted.
     * @return User object with the inserted user's details, or null if insertion fails.
     * @throws SQLException If there is an error during the database operation.
     */
    public User insertUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, full_name, last_session_invalidation) VALUES (?, ?, ?, ?) RETURNING username, password_hash, full_name, last_session_invalidation";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getFullName());
            pstmt.setTimestamp(4, user.getLastSessionInvalidation());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("full_name"),
                            rs.getTimestamp("last_session_invalidation")
                    );
                }
            }
        }
        return null;
    }
}
