package org.titilda.music.ssr.routes;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.model.Song;
import org.titilda.music.base.model.Genre;
import org.titilda.music.base.model.User;
import org.titilda.music.base.database.DatabaseManager;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.model.Playlist;
import org.titilda.music.ssr.BaseAuthenticatedGetServlet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Example concrete implementation of BaseServlet that renders the home page.
 * Demonstrates how to use the abstract servlet pattern with Thymeleaf
 * templates.
 */
@WebServlet(urlPatterns = { "/home" })
public final class HomeServlet extends BaseAuthenticatedGetServlet {

    @Override
    protected String getTemplatePath() {
        return "home"; // This will resolve to templates/home.html
    }

    @Override
    protected Map<String, Object> prepareTemplateVariables(HttpServletRequest request, HttpServletResponse response,
            User user) {
        Map<String, Object> variables = new HashMap<>();

        // Add some example variables
        variables.put("user", user);
        variables.put("currentTime", new java.util.Date());
        variables.put("userAgent", request.getHeader("User-Agent"));

        try (Connection connection = DatabaseManager.getConnection()) {
            variables.put("dbStatus", "Connected to database: " + connection.getMetaData().getURL());

            DAO dao = new DAO(connection);
            List<Playlist> playlists = dao.getPlaylistsOfOwner(user);
            variables.put("playlists", playlists);

            // Fetch user's songs for the new playlist form
            List<Song> userSongs = dao.getSongsOfUser(user);
            variables.put("userSongs", userSongs);

            // we already unwrap the genres here as Strings
            List<String> genres = dao.getGenres().stream().map(Genre::getName).toList();
            variables.put("genres", genres);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // You can add any data your template needs
        // For example: user info, music data, etc.

        return variables;
    }
}
