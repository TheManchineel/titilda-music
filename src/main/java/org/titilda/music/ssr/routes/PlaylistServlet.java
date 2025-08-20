package org.titilda.music.ssr.routes;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.database.DatabaseManager;
import org.titilda.music.base.exceptions.InternalErrorException;
import org.titilda.music.base.model.Playlist;
import org.titilda.music.base.model.Song;
import org.titilda.music.base.model.User;
import org.titilda.music.ssr.BaseAuthenticatedGetServlet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.UUID;

@WebServlet(urlPatterns = { "/playlist" })
public class PlaylistServlet extends BaseAuthenticatedGetServlet {

    @Override
    protected String getTemplatePath() {
        return "playlist";
    }

    @Override
    protected Map<String, Object> prepareTemplateVariables(HttpServletRequest request, HttpServletResponse response,
            User user) throws InternalErrorException {
        Map<String, Object> variables = new HashMap<>();
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
            variables.put("error", "Playlist not found");
            return variables;
        }

        try (Connection connection = DatabaseManager.getConnection()) {
            DAO dao = new DAO(connection);
            Optional<Playlist> playlistOpt = dao.getPlaylistById(playlistId);
            if (playlistOpt.isEmpty()) {
                variables.put("error", "Playlist not found");
                return variables;
            }

            int page = 0;
            String off = request.getParameter("page");
            if (off != null) {
                try {
                    page = Integer.parseInt(off);
                } catch (NumberFormatException _) {}
            }

            Playlist playlist = playlistOpt.get();
            variables.put("playlist", playlist);
            List<Song> songs = dao.getSongsInPlaylist(playlistId, page);
            variables.put("songs", songs);

            // Get songs not in this playlist for the add songs form
            List<Song> songsNotInPlaylist = dao.getSongsNotInPlaylist(user, playlistId);
            variables.put("songsNotInPlaylist", songsNotInPlaylist);

            variables.put("page", page);
            variables.put("pageCount", dao.getSongPageCount(playlistId));

        } catch (SQLException _) {
            throw new InternalErrorException("Internal server error");
        }

        return variables;
    }
}
