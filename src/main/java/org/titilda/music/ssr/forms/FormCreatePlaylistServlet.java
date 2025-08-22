package org.titilda.music.ssr.forms;

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

@WebServlet(urlPatterns = { "/form/create-playlist" })
public class FormCreatePlaylistServlet extends BaseAuthenticatedPostWithRedirectServlet {
    private static final String FAILURE_URL = "/home";

    @Override
    protected String processRequestAndRedirect(HttpServletRequest req, HttpServletResponse resp, User user)
            throws ServletException, IOException {
        String name = req.getParameter("playlistName");
        if (name == null || name.trim().isEmpty()) {
            return FAILURE_URL + "?error=playlist_invalid_name";
        }

        String[] selectedSongIds = req.getParameterValues("songIds");

        if (selectedSongIds == null) {
            selectedSongIds = new String[0];
        }

        try (Connection connection = DatabaseManager.getConnection() ){
            connection.setAutoCommit(false);
            DAO dao = new DAO(connection);

            Playlist playlist = new Playlist(name.trim(), user.getUsername(), false);
            playlist = dao.insertPlaylist(playlist);

            for (String idStr : selectedSongIds) {
                try {
                    UUID songId = UUID.fromString(idStr);
                    dao.addSongToPlaylist(playlist.getId(), songId, user.getUsername());
                } catch (IllegalArgumentException _) {
                    // skip invalid UUIDs silently
                }
            }

            connection.commit();
            return "/playlist?id=" + playlist.getId();
        }
        catch (SQLException _) {
            return FAILURE_URL + "?error=playlist_creation_failed";
        }
    }
}
