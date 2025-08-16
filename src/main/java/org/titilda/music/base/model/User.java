package org.titilda.music.base.model;

import org.titilda.music.base.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Model class representing a User in the music application.
 * <p>
 * MVC: Minchiate Volute Costantemente
 */
public class User {
    private String username;
    private String passwordHash;
    private String fullName;
    private Timestamp lastSessionInvalidation;

    // Default constructor
    public User() {
    }

    // Constructor with all fields
    public User(String username, String passwordHash, String fullName, Timestamp lastSessionInvalidation) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.lastSessionInvalidation = lastSessionInvalidation;
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

    public Timestamp getLastSessionInvalidation() {
        return lastSessionInvalidation;
    }

    public void setLastSessionInvalidation(Timestamp lastSessionInvalidation) {
        this.lastSessionInvalidation = lastSessionInvalidation;
    }

}
