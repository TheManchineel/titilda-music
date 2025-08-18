package org.titilda.music.ssr.routes;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.database.DatabaseManager;
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
        return "playlist"; // templates/playlist.html
    }

    @Override
    protected Map<String, Object> prepareTemplateVariables(HttpServletRequest request, HttpServletResponse response, User user) {
        Map<String, Object> variables = new HashMap<>();

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            // no id parameter provided
            variables.put("error", "Playlist not found");
            return variables;
        }

        UUID playlistId;
        try {
            playlistId = UUID.fromString(idParam);
        } catch (IllegalArgumentException ex) {
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

            Playlist playlist = playlistOpt.get();
            variables.put("playlist", playlist);
            List<Song> songs = dao.getSongsInPlaylist(playlistId);
            variables.put("songs", songs);

            // offset handling
            int offset = 0;
            String off = request.getParameter("offset");
            if (off != null) {
                try { offset = Integer.parseInt(off); } catch (NumberFormatException _) {}
            }
            if (offset > Math.max(0, songs.size() - 5)) offset = Math.max(0, songs.size() - 5);
            if (offset < 0) offset = 0;
            variables.put("offset", offset);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return variables;
    }
}


