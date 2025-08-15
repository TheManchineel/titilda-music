package org.titilda.music.base.model;

import java.sql.Timestamp;
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
}
