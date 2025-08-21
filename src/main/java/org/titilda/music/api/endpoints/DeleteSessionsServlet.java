package org.titilda.music.api.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.titilda.music.api.AuthenticatedJsonDeleteServlet;
import org.titilda.music.base.controller.Authentication;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.model.User;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

@WebServlet(urlPatterns = { "/api/sessions"})
public class DeleteSessionsServlet extends AuthenticatedJsonDeleteServlet {
    @Override
    protected JsonNode processApiRequest(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException, SQLException {
        Iterator<String> pathComponents = getPathComponents(req);
        ensurePathComponentsFinished(pathComponents);
        // invalidate all existing sessions
        try {
            Authentication.invalidateAllSessions(user);
            return new JsonMapper().createObjectNode().put("success", true);
        }
        catch (Authentication.FailedToInvalidateSessionException _) {
            throw new InvalidRequestException("Failed to invalidate sessions", 500);
        }
    }
}
