package org.titilda.music.api.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.titilda.music.api.AuthenticatedJsonRESTServlet;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.model.Genre;
import org.titilda.music.base.model.User;

import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/api/genres")
public class GetGenresServlet extends AuthenticatedJsonRESTServlet {
    @Override
    protected JsonNode processApiGet(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException, SQLException {
        ensurePathComponentsFinished(getPathComponents(req));
        ArrayNode array = new JsonMapper().createArrayNode();
        new DAO(dbConnection).getGenres().stream()
                .map(Genre::getName)
                .forEach(array::add);
        return array;
    }
}
