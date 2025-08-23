package org.titilda.music.api.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.UUID;

@WebServlet(urlPatterns = {"/api/playlists"}) // Expecting /api/playlists
public class PlaylistsRESTServlet extends AuthenticatedJsonRESTServlet {
    // GET /api/playlists
    @Override
    protected JsonNode processApiGet(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException, SQLException {
        ensurePathComponentsFinished(getPathComponents(req));
        List<Playlist> playlists = new DAO(dbConnection).getPlaylistsOfOwner(user);
        return new ObjectMapper().valueToTree(playlists);
    }

    private static class PlaylistCreate {
        public String name;
        public List<UUID> songs;
    }

    // POST /api/playlists
    @Override
    protected JsonNode processApiPost(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException, SQLException {
        ensurePathComponentsFinished(getPathComponents(req));
        DAO dao = new DAO(dbConnection);

        PlaylistCreate playlistCreate = (PlaylistCreate) getJsonModelRequestBody(req, PlaylistCreate.class);
        if (playlistCreate.name == null || playlistCreate.name.isEmpty())
            throw new InvalidRequestException("Playlist name cannot be empty", HttpServletResponse.SC_BAD_REQUEST);
        if (playlistCreate.songs == null)
            playlistCreate.songs = List.of();

        Playlist createdPlaylist = dao.insertPlaylist(new Playlist(playlistCreate.name, user.getUsername(), false));
        for (UUID songId : playlistCreate.songs) {
            if (!dao.addSongToPlaylist(createdPlaylist.getId(), songId, user.getUsername()))
                throw new InvalidRequestException("Song with ID " + songId + " not found or accessible.", HttpServletResponse.SC_NOT_FOUND);
        }

        List<Song> songs = dao.getSongsInPlaylist(createdPlaylist.getId());
        return new ObjectMapper().convertValue(songs, JsonNode.class);
    }
}

