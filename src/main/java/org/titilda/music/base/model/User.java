package org.titilda.music.base.model;

import org.titilda.music.base.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Model class representing a User in the music application.
 *
 * MVC: Minchiate Volute Costantemente
 */
public class User {
    private String username;
    private String passwordHash;
    private String fullName;

    // Default constructor
    public User() {}

    // Constructor with all fields
    public User(String username, String passwordHash, String fullName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    public boolean insert(Connection con) throws SQLException {
        if( this.username == null)
            throw new IllegalArgumentException("Username cannot be null");
        if( this.passwordHash == null)
            throw new IllegalArgumentException("Password hash cannot be null");
        if( this.fullName == null)
            throw new IllegalArgumentException("Full name cannot be null");

        String sql = "INSERT INTO Users (username, password_hash, full_name) VALUES (?, ?, ?) ON CONFLICT (username) DO NOTHING";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, this.getUsername());
            ps.setString(2, this.getPasswordHash());
            ps.setString(3, this.getFullName());
            return ps.executeUpdate() == 1;
        }
    }

}
