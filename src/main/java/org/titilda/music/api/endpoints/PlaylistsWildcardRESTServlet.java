package org.titilda.music.api.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.api.AuthenticatedJsonRESTServlet;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.model.Playlist;
import org.titilda.music.base.model.Song;
import org.titilda.music.base.model.User;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@WebServlet(urlPatterns = {"/api/playlists/*"})
public final class PlaylistsWildcardRESTServlet extends AuthenticatedJsonRESTServlet {
    @Override
    protected JsonNode processApiPost(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException, SQLException {
        Iterator<String> iter = getPathComponents(req);
        String playlistIdStr = getNextPathComponent(iter);
        if (!getNextPathComponent(iter).equals("songs")) {
            throw new InvalidRequestException("Not found", HttpServletResponse.SC_NOT_FOUND);
        }
        ensurePathComponentsFinished(iter);

        UUID playlistId;
        try {
            playlistId = UUID.fromString(playlistIdStr);
        } catch (IllegalArgumentException _) {
            throw new InvalidRequestException("Invalid playlist ID", HttpServletResponse.SC_BAD_REQUEST);
        }

        DAO dao = new DAO(dbConnection);
        List<UUID> uuids = getJsonArrayRequestBody(req).stream().map(idStr -> {
            try {
                return UUID.fromString(idStr);
            } catch (IllegalArgumentException _) {
                return null;
            }
        }).toList();

        for (UUID songId : uuids) {
            boolean result = dao.addSongToPlaylist(playlistId, songId, user.getUsername());
            if (!result) {
                throw new InvalidRequestException("Failed to add song " + songId + " to playlist " + playlistId, HttpServletResponse.SC_BAD_REQUEST);
            }
        }

        return new JsonMapper().createObjectNode().put("status", "ok");
    }

    @Override
    protected JsonNode processApiGet(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException, SQLException {
        Iterator<String> iter = getPathComponents(req);
        String playlistIdStr = getNextPathComponent(iter);
        if(!getNextPathComponent(iter).equals("songs")){
            throw new InvalidRequestException("Not found", HttpServletResponse.SC_NOT_FOUND);
        }
        ensurePathComponentsFinished(iter);

        UUID playlistId;
        try {
            playlistId = UUID.fromString(playlistIdStr);
        } catch (IllegalArgumentException _) {
            throw new InvalidRequestException("Invalid playlist ID", HttpServletResponse.SC_BAD_REQUEST);
        }

        DAO dao = new DAO(dbConnection);
        Playlist playlist = dao.getPlaylistById(playlistId)
                .filter(p -> p.getOwner().equals(user.getUsername()))
                .orElseThrow(() -> new InvalidRequestException("Playlist not found", HttpServletResponse.SC_NOT_FOUND));

        List<Song> songs = dao.getSongsInPlaylist(playlist.getId());
        return new ObjectMapper().valueToTree(songs);
    }

    @Override
    protected JsonNode processApiPut(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException, SQLException {
        Iterator<String> iter = getPathComponents(req);
        String playlistIdStr = getNextPathComponent(iter);
        if(!getNextPathComponent(iter).equals("song-order")){
            throw new InvalidRequestException("Not found", HttpServletResponse.SC_NOT_FOUND);
        }
        ensurePathComponentsFinished(iter);

        UUID playlistId;
        try {
            playlistId = UUID.fromString(playlistIdStr);
        } catch (IllegalArgumentException _) {
            throw new InvalidRequestException("Invalid playlist ID", HttpServletResponse.SC_BAD_REQUEST);
        }

        DAO dao = new DAO(dbConnection);
        dao
                .getPlaylistById(playlistId)
                .map(Playlist::getOwner)
                .filter(owner -> owner.equals(user.getUsername()))
                .orElseThrow(() -> new InvalidRequestException("Playlist not found", HttpServletResponse.SC_NOT_FOUND));

        List<UUID> reorderedSongIds = getJsonArrayRequestBody(req).stream().map(s -> {
            try {
                return UUID.fromString(s);
            }
            catch (IllegalArgumentException _) {
                return null;
            }
        }).distinct().toList();

        List<UUID> currentSongIds = dao.getSongsInPlaylist(playlistId).stream().map(Song::getId).toList();

        if (reorderedSongIds.size() != currentSongIds.size() || !new HashSet<>(reorderedSongIds).equals(new HashSet<>(currentSongIds)))
            throw new InvalidRequestException(
                    "Songs in playlist and reordered array must be the same",
                    HttpServletResponse.SC_BAD_REQUEST
            );

        dao.clearPlaylist(playlistId, true);
        for (UUID songId : reorderedSongIds) {
            dao.addSongToPlaylist(playlistId, songId, user.getUsername());
        }

        List<Song> updatedSongs = dao.getSongsInPlaylist(playlistId);
        return new ObjectMapper().valueToTree(updatedSongs);
    }
}
