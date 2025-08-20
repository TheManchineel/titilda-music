package org.titilda.music.base.database;

import org.titilda.music.base.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DAO {
    private static final int PLAYLIST_SONG_PAGE_SIZE = 5;

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
     * @return User object with the inserted user's details, or null if insertion
     *         fails.
     * @throws SQLException If there is an error during the database operation.
     */
    public Optional<User> insertUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, full_name, last_session_invalidation) VALUES (?, ?, ?, ?) RETURNING username, password_hash, full_name, last_session_invalidation";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setTimestamp(4, user.getLastSessionInvalidation());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("full_name"),
                            rs.getTimestamp("last_session_invalidation")));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(
                            new User(rs.getString("username"), rs.getString("password_hash"), rs.getString("full_name"),
                                    rs.getTimestamp("last_session_invalidation")));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves all songs owned by a specific user.
     * This method uses a prepared statement to prevent SQL injection.
     *
     * @param user User object whose songs are to be retrieved.
     * @return List of Song objects owned by the user, ordered by title and release
     *         year.
     * @throws SQLException If there is an error during the database operation.
     */
    public List<Song> getSongsOfUser(User user) throws SQLException {
        if (user == null || user.getUsername() == null) {
            throw new IllegalArgumentException("User cannot be null and must have a valid username");
        }
        String sql = "SELECT * FROM songs WHERE owner = ? ORDER BY title, release_year DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            return mapResultSetToSongList(ps);
        }
    }

    /**
     * Retrieves all songs in a specific playlist.
     * This method uses a prepared statement to prevent SQL injection.
     *
     * @param playlistId UUID of the playlist whose songs are to be retrieved.
     * @return List of Song objects in the specified playlist, ordered by position.
     * @throws SQLException If there is an error during the database operation.
     */
    public List<Song> getSongsInPlaylist(UUID playlistId, int page) throws SQLException {
        String sql = "SELECT s.* FROM songs s " +
                "JOIN playlistsongs ps ON s.id = ps.song_id " +
                "WHERE ps.playlist_id = ? ORDER BY ps.position " +
                "LIMIT ? OFFSET ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, playlistId);
            ps.setInt(2, PLAYLIST_SONG_PAGE_SIZE);
            ps.setInt(3, page * PLAYLIST_SONG_PAGE_SIZE);
            return mapResultSetToSongList(ps);
        }
    }

    /**
     * Calculates the total number of pages required to display all songs in a playlist,
     * given a fixed page size.
     *
     * @param playlistId UUID of the playlist whose song page count is to be calculated.
     * @return The number of pages required to display all songs in the specified playlist.
     * @throws SQLException If there is an error during the database operation.
     */
    public int getSongPageCount(UUID playlistId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM playlistsongs WHERE playlist_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return (int) Math.ceil((double) rs.getInt(1) / PLAYLIST_SONG_PAGE_SIZE);
                }
                return 0;
            }
        }
    }

    /**
     * Retrieves all songs owned by a user that are not in a specific playlist.
     * This method uses a prepared statement to prevent SQL injection.
     *
     * @param user       User object whose songs are to be retrieved.
     * @param playlistId UUID of the playlist to exclude songs from.
     * @return List of Song objects owned by the user that are not in the specified
     *         playlist, ordered by title.
     * @throws SQLException If there is an error during the database operation.
     */
    public List<Song> getSongsNotInPlaylist(User user, UUID playlistId) throws SQLException {
        // TODO: replace owner passing with subquery
        if (user == null || user.getUsername() == null) {
            throw new IllegalArgumentException("User cannot be null and must have a valid username");
        }
        String sql = "SELECT * FROM songs WHERE owner = ? AND id NOT IN " +
                "(SELECT song_id FROM playlistsongs WHERE playlist_id = ?) ORDER BY title";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setObject(2, playlistId);
            return mapResultSetToSongList(ps);
        }
    }

    /**
     * Private method to create a list of Song objects from a ResultSet.
     * This method is used internally to convert the result set into a list of Song
     * objects.
     *
     * @param ps PreparedStatement that has been executed.
     * @return List of Song objects created from the result set.
     * @throws SQLException If there is an error during the database operation.
     */
    private List<Song> mapResultSetToSongList(PreparedStatement ps) throws SQLException {
        ResultSet rs = ps.executeQuery();
        List<Song> songs = new ArrayList<>();
        while (rs.next()) {
            songs.add(new Song(
                    (UUID) rs.getObject("id"),
                    rs.getString("title"),
                    rs.getString("album"),
                    rs.getString("artist"),
                    rs.getString("audio_mime_type"),
                    rs.getInt("release_year"),
                    rs.getString("genre"),
                    rs.getString("owner")));
        }
        return songs;
    }

    /**
     * Private method to create a Song object from a ResultSet.
     * This method is used internally to convert a single result set row into a Song
     * object.
     *
     * @param rs ResultSet containing the song data.
     * @return Song object created from the result set.
     * @throws SQLException If there is an error during the database operation.
     */
    private Song mapResultSetToSong(ResultSet rs) throws SQLException {
        return new Song(
                (UUID) rs.getObject("id"),
                rs.getString("title"),
                rs.getString("album"),
                rs.getString("artist"),
                rs.getString("audio_mime_type"),
                rs.getInt("release_year"),
                rs.getString("genre"),
                rs.getString("owner"));
    }

    /**
     * Private method to create a Playlist object from a ResultSet.
     * This method is used internally to convert a single result set row into a
     * Playlist object.
     *
     * @param rs ResultSet containing the playlist data.
     * @return Playlist object created from the result set.
     * @throws SQLException If there is an error during the database operation.
     */
    private Playlist mapResultSetToPlaylist(ResultSet rs) throws SQLException {
        return new Playlist(
                (UUID) rs.getObject("id"),
                rs.getString("name"),
                rs.getString("owner"),
                rs.getTimestamp("created_at"),
                rs.getBoolean("is_manually_sorted"));
    }

    public Song insertSong(Song song) throws SQLException {
        String sql = "INSERT INTO songs (id, title, album, artist, audio_mime_type, release_year, genre, owner) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING *";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, song.getId());
            ps.setString(2, song.getTitle());
            ps.setString(3, song.getAlbum());
            ps.setString(4, song.getArtist());
            ps.setString(5, song.getAudioMimeType());
            ps.setObject(6, song.getReleaseYear());
            ps.setString(7, song.getGenre());
            ps.setString(8, song.getOwner());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSong(rs);
                } else {
                    throw new SQLException("Failed to insert song, no rows returned");
                }
            }
        }
    }

    /**
     * Adds a song to a playlist by appending it at the next available position.
     * This method calculates the next position based on the current maximum.
     *
     * @param playlistId The playlist to add the song to
     * @param songId     The song to be added
     * @return true if the song was successfully added, false otherwise
     * @throws SQLException If any database error occurs
     */
    public boolean addSongToPlaylist(UUID playlistId, UUID songId) throws SQLException {
        try (PreparedStatement checkPs = connection.prepareStatement("SELECT song_id FROM playlistsongs WHERE playlist_id = ? AND song_id = ? LIMIT 1")) {
            checkPs.setObject(1, playlistId);
            checkPs.setObject(2, songId);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.isBeforeFirst()) {
                    // there already is a song
                    return false;
                }
            }
        }

        int nextPosition = 0;
        // find the next available position in the playlist
        // if there are no songs (position is null), start at 0
        String posSql = "SELECT COALESCE(MAX(position) + 1, 0) AS next_pos FROM playlistsongs WHERE playlist_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(posSql)) {
            ps.setObject(1, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    nextPosition = rs.getInt("next_pos");
                }
            }
        }

        String insertSql = "INSERT INTO playlistsongs (playlist_id, song_id, position) VALUES (?, ?, ?) RETURNING *";
        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            ps.setObject(1, playlistId);
            ps.setObject(2, songId);
            ps.setInt(3, nextPosition);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        throw new SQLException("Failed to add song to playlist");
    }

    /**
     * Retrieves all playlists owned by a specific user.
     * This method uses a prepared statement to prevent SQL injection.
     *
     * @param user User object whose playlists are to be retrieved.
     * @return ArrayList of Playlist objects owned by the user, ordered by creation
     *         date and name.
     */
    public List<Playlist> getPlaylistsOfOwner(User user) throws SQLException {
        String sql = "SELECT id, name, owner, created_at, is_manually_sorted FROM playlists WHERE owner = ? ORDER BY created_at DESC, name";
        List<Playlist> playlists = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
        }
        return playlists;
    }

    public Optional<Playlist> getPlaylistById(UUID playlistId) throws SQLException {
        String sql = "SELECT id, name, owner, created_at, is_manually_sorted FROM playlists WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Playlist(
                            (UUID) rs.getObject("id"),
                            rs.getString("name"),
                            rs.getString("owner"),
                            rs.getTimestamp("created_at"),
                            rs.getBoolean("is_manually_sorted")));
                }
            }
        }
        return Optional.empty();
    }

    public Playlist insertPlaylist(Playlist playlist) throws SQLException {
        String sql = "INSERT INTO playlists (id, name, owner, created_at, is_manually_sorted) VALUES (?, ?, ?, ?, ?) RETURNING *";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, playlist.getId());
            ps.setString(2, playlist.getName());
            ps.setString(3, playlist.getOwner());
            ps.setTimestamp(4, playlist.getCreatedAt());
            ps.setBoolean(5, playlist.isManuallySorted());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPlaylist(rs);
                } else {
                    throw new SQLException("Failed to insert playlist, no rows returned");
                }
            }
        }
    }

    public List<Genre> getGenres() throws SQLException {
        String sql = "SELECT name FROM genres ORDER BY name";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                List<Genre> genres = new ArrayList<>();
                while (rs.next()) {
                    genres.add(new Genre(rs.getString("name")));
                }
                return genres;
            }
        }
    }

    /**
     * Retrieves a song from the database by its UUID.
     *
     * @param songId UUID of the song to be retrieved.
     * @return Optional containing the Song object if found, empty otherwise.
     * @throws SQLException If there is an error during the database operation.
     */
    public Optional<Song> getSongById(UUID songId) throws SQLException {
        String sql = "SELECT * FROM songs WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, songId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSong(rs));
                }
            }
        }
        return Optional.empty();
    }
}
