package org.titilda.music.ssr.routes;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.database.DatabaseManager;
import org.titilda.music.ssr.exceptions.InternalErrorException;
import org.titilda.music.base.model.Song;
import org.titilda.music.base.model.User;
import org.titilda.music.ssr.BaseAuthenticatedGetServlet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@WebServlet(urlPatterns = { "/song" })
public final class SongServlet extends BaseAuthenticatedGetServlet {

    @Override
    protected String getTemplatePath() {
        return "song"; // templates/song.html
    }

    @Override
    protected Map<String, Object> prepareTemplateVariables(HttpServletRequest request, HttpServletResponse response,
            User user) throws InternalErrorException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            // no id parameter provided
            variables.put("error", "Song not found");
            return variables;
        }

        UUID songId;
        try {
            songId = UUID.fromString(idParam);
        } catch (IllegalArgumentException ex) {
            // invalid UUID format
            variables.put("error", "Song not found");
            return variables;
        }

        try (Connection connection = DatabaseManager.getConnection()) {
            DAO dao = new DAO(connection);
            Optional<Song> songOpt = dao.getSongById(songId);
            if (songOpt.isEmpty()) {
                variables.put("error", "Song not found");
                return variables;
            }

            Song song = songOpt.get();

            // Check ownership - user can only view their own songs
            if (!song.getOwner().equals(user.getUsername())) {
                variables.put("error", "Song not found");
                return variables;
            }

            variables.put("song", song);
        } catch (SQLException _) {
            throw new InternalErrorException("Internal server error");
        }

        return variables;
    }
}
