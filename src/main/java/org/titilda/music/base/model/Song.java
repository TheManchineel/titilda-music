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
    public Song() {}

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(id, song.id);
    }

    public boolean insert(Connection con) throws SQLException {
        if (this.id == null)
            throw new IllegalArgumentException("ID cannot be null");
        if (this.title == null || this.title.isEmpty())
            throw new IllegalArgumentException("Title cannot be null or empty");

        String sql = "INSERT INTO songs (id, title, album, artist, artwork, audio_file, audio_mime_type, release_year, genre, owner) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, this.id);
            ps.setString(2, this.title);
            ps.setString(3, this.album);
            ps.setString(4, this.artist);
            ps.setString(5, this.artwork);
            ps.setString(6, this.audioFile);
            ps.setString(7, this.audioMimeType);
            ps.setObject(8, this.releaseYear);
            ps.setString(9, this.genre);
            ps.setString(10, this.owner);

            return ps.executeUpdate() > 0;
        }
    }

    public static List<Song> getSongsInPlaylist(Connection con, UUID playlistId) throws SQLException {
        String sql = "SELECT s.* FROM songs s " +
                     "JOIN playlistsongs ps ON s.id = ps.song_id " +
                     "WHERE ps.playlist_id = ? ORDER BY ps.position";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, playlistId);
            ResultSet rs = ps.executeQuery();
            List<Song> songs = new ArrayList<>();
            while (rs.next()) {
                songs.add(new Song(
                    (UUID) rs.getObject("id"),
                    rs.getString("title"),
                    rs.getString("album"),
                    rs.getString("artist"),
                    rs.getString("artwork"),
                    rs.getString("audio_file"),
                    rs.getString("audio_mime_type"),
                    rs.getInt("release_year"),
                    rs.getString("genre"),
                    rs.getString("owner")
                ));
            }
            return songs;
        }
    }

    public static List<Song> getSongsNotInPlaylist(Connection con, User user, UUID playlistId) throws SQLException {
        if (user == null || user.getUsername() == null) {
            throw new IllegalArgumentException("User cannot be null and must have a valid username");
        }

        String sql = "SELECT * FROM songs WHERE owner = ? AND id NOT IN " +
                     "(SELECT song_id FROM playlistsongs WHERE playlist_id = ?) ORDER BY title";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setObject(2, playlistId);
            ResultSet rs = ps.executeQuery();
            List<Song> songs = new ArrayList<>();
            while (rs.next()) {
                songs.add(new Song(
                    (UUID) rs.getObject("id"),
                    rs.getString("title"),
                    rs.getString("album"),
                    rs.getString("artist"),
                    rs.getString("artwork"),
                    rs.getString("audio_file"),
                    rs.getString("audio_mime_type"),
                    rs.getInt("release_year"),
                    rs.getString("genre"),
                    rs.getString("owner")
                ));
            }
            return songs;
        }
    }


}
