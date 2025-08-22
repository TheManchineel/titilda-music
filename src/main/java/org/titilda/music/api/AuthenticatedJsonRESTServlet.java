package org.titilda.music.api;

import com.fasterxml.jackson.core.type.TypeReference;
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

    protected final String getNextPathComponent(Iterator<String> iter) throws InvalidRequestException {
        if (!iter.hasNext()) {
            throw new InvalidRequestException("Not found", HttpServletResponse.SC_NOT_FOUND);
        }
        return iter.next();
    }

    protected static List<String> getJsonArrayRequestBody(HttpServletRequest req) throws InvalidRequestException {
        if (!"application/json".equalsIgnoreCase(req.getContentType())) {
            throw new InvalidRequestException("Expected application/json content type", HttpServletResponse.SC_BAD_REQUEST);
        }
        try {
            return new JsonMapper().readValue(req.getInputStream(), new TypeReference<List<String>>() {
            });
        } catch (IOException e) {
            throw new InvalidRequestException("Invalid JSON data", HttpServletResponse.SC_BAD_REQUEST);
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

    private static InvalidRequestException unsupportMethod() throws InvalidRequestException {
        return new InvalidRequestException("Method not supported", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    // ================ STUBBED METHODS ================
    protected JsonNode processApiGet(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException, IOException, SQLException {
        throw unsupportMethod();
    }
    protected JsonNode processApiPost(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException, IOException, SQLException {
        throw unsupportMethod();
    }
    protected JsonNode processApiPut(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException, IOException, SQLException {
        throw unsupportMethod();
    }
    protected JsonNode processApiDelete(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException, IOException, SQLException {
        throw unsupportMethod();
    }
    protected JsonNode processApiPatch(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException, IOException, SQLException {
        throw unsupportMethod();
    }
    // =================================================

    private static User checkBearerToken(HttpServletRequest req) throws InvalidRequestException {
        return Optional
                .ofNullable(req.getHeader("Authorization"))
                .filter(header -> header.toLowerCase().startsWith("bearer "))
                .map(header -> header.substring("Bearer ".length()))
                .flatMap(Authentication::validateToken)
                .orElseThrow(() -> new InvalidRequestException("Invalid token", HttpServletResponse.SC_UNAUTHORIZED));
    }

    protected final void handleHttpRequest(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        JsonNode jsonNode;
        try (Connection dbConnection = DatabaseManager.getConnection()) {
            dbConnection.setAutoCommit(false);
            User user = checkBearerToken(req);

            jsonNode = switch (req.getMethod()) {
                case "GET" -> processApiGet(user, req, dbConnection);
                case "POST" -> processApiPost(user, req, dbConnection);
                case "DELETE" -> processApiDelete(user, req, dbConnection);
                case "PUT" -> processApiPut(user, req, dbConnection);
                case "PATCH" -> processApiPatch(user, req, dbConnection);
                default -> throw unsupportMethod();
            };
            dbConnection.commit();
        } catch (InvalidRequestException e) {
            resp.setStatus(e.getStatus());
            jsonNode = new JsonMapper().createObjectNode().put("error", e.getError());
        } catch (SQLException | IOException _) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonNode = new JsonMapper().createObjectNode().put("error", "Temporary error processing request");
        }

        String jsonResponseText = jsonNode.toString();
        try {
            resp.getWriter().println(jsonResponseText);
        } catch (IOException _) {
            System.out.println("Error writing JSON response to socket.");
        }
    }

    // ================ DEFAULT JAKARTA OVERRIDES ================
    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) {
        handleHttpRequest(req, resp);
    }
    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) {
        handleHttpRequest(req, resp);
    }
    @Override
    protected final void doPut(HttpServletRequest req, HttpServletResponse resp) {
        handleHttpRequest(req, resp);
    }
    @Override
    protected final void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        handleHttpRequest(req, resp);
    }
    @Override
    protected final void doPatch(HttpServletRequest req, HttpServletResponse resp) {
        handleHttpRequest(req, resp);
    }
    //
}
