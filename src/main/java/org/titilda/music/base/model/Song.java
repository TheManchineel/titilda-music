package org.titilda.music.base.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Model class representing a Song in the music application.
 */
public class Song {
    private UUID id;
    private String title;
    private String album;
    private String artist;
    private String artwork;
    private String audioFile;
    private String audioMimeType;
    private Integer releaseYear;
    private String genre;
    private String owner;

    // Default constructor
    public Song() {
    }

    // Constructor with all fields except id (for new songs)
    public Song(String title, String album, String artist, String artwork,
                String audioFile, String audioMimeType, Integer releaseYear,
                String genre, String owner) {
        this.id = UUID.randomUUID(); // Auto-generate ID for new songs
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.artwork = artwork;
        this.audioFile = audioFile;
        this.audioMimeType = audioMimeType;
        this.releaseYear = releaseYear;
        this.genre = genre;
        this.owner = owner;
    }

    // Constructor with all fields including id (for existing songs)
    public Song(UUID id, String title, String album, String artist, String artwork,
                String audioFile, String audioMimeType, Integer releaseYear,
                String genre, String owner) {
        this.id = id;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.artwork = artwork;
        this.audioFile = audioFile;
        this.audioMimeType = audioMimeType;
        this.releaseYear = releaseYear;
        this.genre = genre;
        this.owner = owner;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getArtwork() {
        return artwork;
    }

    public void setArtwork(String artwork) {
        this.artwork = artwork;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    public String getAudioMimeType() {
        return audioMimeType;
    }

    public void setAudioMimeType(String audioMimeType) {
        this.audioMimeType = audioMimeType;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

}
