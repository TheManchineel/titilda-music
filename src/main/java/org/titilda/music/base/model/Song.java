package org.titilda.music.base.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.titilda.music.base.model.mimetypes.SongMimeType;

/**
 * Model class representing a Song in the music application.
 */
public final class Song {
    private UUID id;
    private String title;
    private String album;
    private String artist;
    private String audioMimeType;
    private Integer releaseYear;
    private String genre;
    private String owner;

    // Default constructor
    public Song() {
    }

    // Constructor with all fields including id
    public Song(UUID id, String title, String album, String artist, String audioMimeType, Integer releaseYear,
            String genre, String owner) {
        this.id = id;
        this.title = title;
        this.album = album;
        this.artist = artist;
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

    public String getArtworkUrl() {
        return "/static/artworks/" + id + ".webp";
    }

    public String getAudioUrl() {
        return "/static/songs/" + id + "." + SongMimeType.fromMimeType(audioMimeType).getExtension();
    }
}
