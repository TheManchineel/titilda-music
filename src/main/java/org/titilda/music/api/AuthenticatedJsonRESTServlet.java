package org.titilda.music.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import org.titilda.music.base.controller.Authentication;
import org.titilda.music.base.database.DatabaseManager;
import org.titilda.music.base.model.User;

public abstract class AuthenticatedJsonRESTServlet extends HttpServlet {
    protected final Iterator<String> getPathComponents(HttpServletRequest req) {
        String subPath = req.getRequestURI().substring(req.getServletPath().length());
        return subPath.isEmpty() ? Collections.emptyIterator() : Arrays.stream(subPath.split("/")).filter(s -> !s.isBlank()).iterator();
    }

    protected final void ensurePathComponentsFinished(Iterator<String> iter) throws InvalidRequestException {
        if (iter.hasNext()) {
            throw new InvalidRequestException("Not found", HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public static class InvalidRequestException extends Exception {
        private final int status;
        private final String error;

        public InvalidRequestException(String error, int status) {
            this.error = error;
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }
    }

    protected abstract JsonNode processApiRequest(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException, IOException, SQLException;

    private static User checkBearerToken(HttpServletRequest req) throws InvalidRequestException {
        return Optional
                .ofNullable(req.getHeader("Authorization"))
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring("Bearer ".length()))
                .flatMap(Authentication::validateToken)
                .orElseThrow(() -> new InvalidRequestException("Invalid token", HttpServletResponse.SC_UNAUTHORIZED));
    }

    protected final void handleHttpRequest(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        JsonNode jsonNode;

        try (Connection dbConnection = DatabaseManager.getConnection()) {
            jsonNode = processApiRequest(checkBearerToken(req), req, dbConnection);
        }
        catch (InvalidRequestException e) {
            resp.setStatus(e.getStatus());
            jsonNode = new JsonMapper().createObjectNode().put("error", e.getError());
        }
        catch (SQLException | IOException _) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonNode = new JsonMapper().createObjectNode().put("error", "Temporary error processing request");
        }

        String jsonResponseText = jsonNode.toString();
        try {
            resp.getWriter().write(jsonResponseText);
        }
        catch (IOException _) {
            System.out.println("Error writing JSON response to socket.");
        }
    }
}
