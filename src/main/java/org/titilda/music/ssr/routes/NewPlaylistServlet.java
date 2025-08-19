package org.titilda.music.ssr.routes;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.database.DatabaseManager;
import org.titilda.music.base.model.Playlist;
import org.titilda.music.base.model.User;
import org.titilda.music.ssr.BaseAuthenticatedPostWithRedirectServlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet(urlPatterns = {"/new-playlist"})
public class NewPlaylistServlet extends BaseAuthenticatedPostWithRedirectServlet {
    private static final String REDIRECT_URL = "/home";

    @Override
    protected String processRequestAndRedirect(HttpServletRequest req, HttpServletResponse resp, User user) throws ServletException, IOException {
        String name = req.getParameter("playlistName");
        if (name == null || name.trim().isEmpty()) {
            return REDIRECT_URL;
        }

        String[] selectedSongIds = req.getParameterValues("songIds");

        Connection connection = null;
        try {
            connection = DatabaseManager.getConnection();
            connection.setAutoCommit(false);
            DAO dao = new DAO(connection);

            Playlist playlist = new Playlist(name.trim(), user.getUsername(), false);
            playlist = dao.insertPlaylist(playlist);

            if (selectedSongIds != null) {
                for (String idStr : selectedSongIds) {
                    try {
                        UUID songId = UUID.fromString(idStr);
                        dao.addSongToPlaylist(playlist.getId(), songId);
                    } catch (IllegalArgumentException _) {
                        // skip invalid UUIDs silently
                    }
                }
            }

            connection.commit();
            return "/playlist?id=" + playlist.getId();
        } catch (SQLException e) {
            if (connection != null) {
                try { connection.rollback(); } catch (SQLException ignored) {}
            }
            return REDIRECT_URL;
        } finally {
            if (connection != null) {
                try { connection.close(); } catch (SQLException ignored) {}
            }
        }
    }
}
