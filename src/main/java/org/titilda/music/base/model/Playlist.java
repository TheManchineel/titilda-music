package org.titilda.music.base.model;

import org.titilda.music.base.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Model class representing a Playlist in the music application.
 */
public class Playlist {
    private UUID id;
    private String name;
    private String owner;
    private Timestamp createdAt;
    private boolean isManuallySorted;

    // Default constructor
    public Playlist() {}

    // Constructor with all fields except id and createdAt (for new playlists)
    public Playlist(String name, String owner, boolean isManuallySorted) {
        this.id = UUID.randomUUID(); // Auto-generate ID for new playlists
        this.name = name;
        this.owner = owner;
        this.createdAt = new Timestamp(System.currentTimeMillis()); // Auto-generate timestamp
        this.isManuallySorted = isManuallySorted;
    }

    // Constructor with all fields including id and createdAt (for existing playlists)
    public Playlist(UUID id, String name, String owner, Timestamp createdAt, boolean isManuallySorted) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.createdAt = createdAt;
        this.isManuallySorted = isManuallySorted;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isManuallySorted() {
        return isManuallySorted;
    }

    public void setManuallySorted(boolean manuallySorted) {
        isManuallySorted = manuallySorted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist playlist = (Playlist) o;
        return Objects.equals(id, playlist.id);
    }

    public List<Playlist> getPlaylistsByOwner(User user, Connection con) {
        if (user == null || user.getUsername() == null) {
            throw new IllegalArgumentException("User cannot be null and must have a valid username");
        }

        String sql = "SELECT id, name, owner, created_at, is_manually_sorted FROM playlists WHERE owner = ? ORDER BY created_at DESC, name";
        List<Playlist> playlists = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID id = (UUID) rs.getObject("id");
                    String name = rs.getString("name");
                    String owner = rs.getString("owner");
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    boolean isManuallySorted = rs.getBoolean("is_manually_sorted");
                    playlists.add(new Playlist(id, name, owner, createdAt, isManuallySorted));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch playlists for owner: " + user.getUsername(), e);
        }
        return playlists;
    }
}
