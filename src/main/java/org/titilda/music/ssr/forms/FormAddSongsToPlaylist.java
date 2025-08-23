package org.titilda.music.ssr.forms;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.database.DatabaseManager;
import org.titilda.music.base.model.User;
import org.titilda.music.ssr.BaseAuthenticatedPostWithRedirectServlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet(urlPatterns = { "/form/add-songs-to-playlist" })
public final class FormAddSongsToPlaylist extends BaseAuthenticatedPostWithRedirectServlet {
    @Override
    protected String processRequestAndRedirect(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        String playlistIdString = request.getParameter("id");
        if (playlistIdString == null) {
            return "/error?error=bad_request";
        }
        UUID playlistId;
        try {
            playlistId = UUID.fromString(playlistIdString);
        } catch (IllegalArgumentException ex) {
            return "/error?error=bad_request";
        }

        String[] songIds = request.getParameterValues("songIds");
        if (songIds == null) {
            return "/playlist?id=" + playlistId;
        }

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            DAO dao = new DAO(connection);
            for (String idStr : songIds) {
                try {
                    UUID songId = UUID.fromString(idStr);
                    dao.addSongToPlaylist(playlistId, songId, user.getUsername());
                } catch (IllegalArgumentException _) {
                    // ignore invalid UUIDs
                }
            }

            connection.commit();
        } catch (SQLException _) {
            return "/error?error=internal_server_error";
        }

        return "/playlist?id=" + playlistId;
    }
}
