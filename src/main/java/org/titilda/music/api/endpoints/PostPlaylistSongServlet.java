package org.titilda.music.api.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.api.AuthenticatedJsonPostServlet;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.model.User;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@WebServlet(urlPatterns = {"/api/playlists/*"}) // Expecting /api/playlists/{playlistId}/songs
public class PostPlaylistSongServlet extends AuthenticatedJsonPostServlet {
    @Override
    protected JsonNode processApiRequest(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException, SQLException {
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
}
