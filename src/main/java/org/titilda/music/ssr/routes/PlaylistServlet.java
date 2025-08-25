package org.titilda.music.ssr.routes;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.database.DatabaseManager;
import org.titilda.music.ssr.exceptions.InternalErrorException;
import org.titilda.music.base.model.Playlist;
import org.titilda.music.base.model.Song;
import org.titilda.music.base.model.User;
import org.titilda.music.ssr.BaseAuthenticatedGetServlet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.UUID;

@WebServlet(urlPatterns = { "/playlist" })
public final class PlaylistServlet extends BaseAuthenticatedGetServlet {
    private static class PlaylistNotFoundException extends Exception {}

    @Override
    protected String getTemplatePath() {
        return "playlist";
    }

    @Override
    protected Map<String, Object> prepareTemplateVariables(HttpServletRequest request, HttpServletResponse response,
            User user) throws InternalErrorException {
        Map<String, Object> variables = new HashMap<>();
        try {
            variables.put("user", user);

            String idParam = request.getParameter("id");
            if (idParam == null || idParam.isEmpty()) {
                // no id parameter provided
                variables.put("error", "Playlist not found");
                return variables;
            }

            UUID playlistId;
            try {
                playlistId = UUID.fromString(idParam);
            } catch (IllegalArgumentException _) {
                // invalid UUID format
                throw new PlaylistNotFoundException();
            }

            try (Connection connection = DatabaseManager.getConnection()) {
                DAO dao = new DAO(connection);
                Playlist playlist = dao.getPlaylistById(playlistId)
                        .filter(p -> p.getOwner().equals(user.getUsername()))
                        .orElseThrow(PlaylistNotFoundException::new);

                int page = 0;
                int totalPageCount = dao.getSongPageCount(playlistId);
                String pageStringValue = request.getParameter("page");
                if (pageStringValue != null) {
                    if (totalPageCount == 0) {
                        throw new InternalErrorException("Playlist is empty, cannot access pages", "/error?error=bad_request");
                    }
                    try {
                        page = Integer.parseInt(pageStringValue);
                    } catch (NumberFormatException _) {
                    }
                }

                if (page < 0 || (totalPageCount > 0 && page >= totalPageCount)) {
                    throw new InternalErrorException("Invalid page number", "/error?error=bad_request");
                }

                variables.put("playlist", playlist);
                List<Song> songs = dao.getSongsInPlaylistPaginated(playlistId, page);
                variables.put("songs", songs);

                // Get songs not in this playlist for the add-songs form
                List<Song> songsNotInPlaylist = dao.getSongsNotInPlaylist(user, playlistId);
                variables.put("songsNotInPlaylist", songsNotInPlaylist);

                variables.put("page", page);
                variables.put("pageCount", dao.getSongPageCount(playlistId));
            } catch (SQLException _) {
                throw new InternalErrorException("Internal server error");
            }

            return variables;
        }
        catch (PlaylistNotFoundException _) {
            throw new InternalErrorException("Playlist not found", "/error?error=not_found");
        }
    }
}
