package org.titilda.music.api.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.titilda.music.api.AuthenticatedJsonRESTServlet;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.model.Playlist;
import org.titilda.music.base.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet(urlPatterns = {"/api/playlists"}) // Expecting /api/playlists
public class GetPlaylistsServlet extends AuthenticatedJsonRESTServlet {

    @Override
    protected JsonNode processApiGet(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException, SQLException {
        ensurePathComponentsFinished(getPathComponents(req));
        List<Playlist> playlists = new DAO(dbConnection).getPlaylistsOfOwner(user);
        return new ObjectMapper().valueToTree(playlists);
    }
}

