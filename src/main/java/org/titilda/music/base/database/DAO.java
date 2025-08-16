package org.titilda.music.base.database;

import org.titilda.music.base.model.Playlist;
import org.titilda.music.base.model.PlaylistSong;
import org.titilda.music.base.model.Song;
import org.titilda.music.base.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public List<Song> getSongsInPlaylist(UUID playlistId) throws SQLException {
        String sql = "SELECT s.* FROM songs s " +
                "JOIN playlistsongs ps ON s.id = ps.song_id " +
                "WHERE ps.playlist_id = ? ORDER BY ps.position";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, playlistId);
            return mapResultSetToSongList(ps);
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
                    rs.getString("artwork"),
                    rs.getString("audio_file"),
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
                rs.getString("artwork"),
                rs.getString("audio_file"),
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
        String sql = "INSERT INTO songs (id, title, album, artist, artwork, audio_file, audio_mime_type, release_year, genre, owner) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING *";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, song.getId());
            ps.setString(2, song.getTitle());
            ps.setString(3, song.getAlbum());
            ps.setString(4, song.getArtist());
            ps.setString(5, song.getArtwork());
            ps.setString(6, song.getAudioFile());
            ps.setString(7, song.getAudioMimeType());
            ps.setObject(8, song.getReleaseYear());
            ps.setString(9, song.getGenre());
            ps.setString(10, song.getOwner());

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
     * Private method to create a PlaylistSong object from a ResultSet.
     * This method is used internally to convert a single result set row into a
     * PlaylistSong object.
     *
     * @param rs ResultSet containing the playlist song data.
     * @return PlaylistSong object created from the result set.
     * @throws SQLException If there is an error during the database operation.
     */
    private PlaylistSong mapResultSetToPlaylistSong(ResultSet rs) throws SQLException {
        return new PlaylistSong(
                rs.getInt("id"),
                (UUID) rs.getObject("playlist_id"),
                (UUID) rs.getObject("song_id"),
                rs.getInt("position"));
    }

    /**
     * Inserts a new PlaylistSong into the database. (This represents a song in a
     * playlist.)
     * This method uses a prepared statement to prevent SQL injection.
     *
     * @param songInPlaylist PlaylistSong object containing the details of the song
     *                       in the playlist.
     * @return PlaylistSong object with the inserted song's details, including
     *         generated ID.
     * @throws SQLException If there is an error during the database operation.
     */
    public PlaylistSong insertPlaylistSong(PlaylistSong songInPlaylist) throws SQLException {
        String sql = "INSERT INTO playlistsongs (playlist_id, song_id, position) VALUES (?, ?, ?) RETURNING *";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, songInPlaylist.getPlaylistId());
            ps.setObject(2, songInPlaylist.getSongId());
            ps.setInt(3, songInPlaylist.getPosition());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPlaylistSong(rs);
                } else {
                    throw new SQLException("Failed to insert playlist song, no rows returned");
                }
            }
        }
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
}
