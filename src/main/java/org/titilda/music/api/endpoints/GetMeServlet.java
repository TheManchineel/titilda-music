package org.titilda.music.api.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.titilda.music.api.AuthenticatedJsonRESTServlet;
import org.titilda.music.base.model.User;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/api/me"}) // Expecting /api/me
public class GetMeServlet extends AuthenticatedJsonRESTServlet {

    @Override
    protected JsonNode processApiGet(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException, IOException, SQLException {
        ensurePathComponentsFinished(getPathComponents(req));
        return new JsonMapper().createObjectNode()
                .put("username", user.getUsername())
                .put("fullName", user.getFullName());
    }
}
