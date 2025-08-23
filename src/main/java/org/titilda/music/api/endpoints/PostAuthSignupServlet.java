package org.titilda.music.api.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.controller.Authentication;
import org.titilda.music.base.model.User;

import static org.titilda.music.api.AuthenticatedJsonRESTServlet.InvalidRequestException;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import static org.titilda.music.api.utils.JsonManipulation.createJwtResponse;
import static org.titilda.music.api.utils.JsonManipulation.ensureStringField;

@WebServlet(urlPatterns = {"/api/auth/signup"})
public class PostAuthSignupServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {

            resp.setContentType("application/json");
            JsonNode node;
            if (!"application/json".equalsIgnoreCase(req.getContentType())) {
                throw new InvalidRequestException("Expected application/json content type", HttpServletResponse.SC_BAD_REQUEST);
            }

            try {
                node = new ObjectMapper().readTree(req.getInputStream());
            } catch (IOException _) {
                throw new InvalidRequestException("Invalid JSON data", HttpServletResponse.SC_BAD_REQUEST);
            }

            String username = ensureStringField(node, "username");
            String password = ensureStringField(node, "password");
            String fullName = ensureStringField(node, "fullName");

            Date now = new Date();
            now = new Date(now.getTime() / 1000L * 1000L);
            User tenativeNewUser = new User(username, password, fullName, new Timestamp(now.getTime()));
            try {
                Authentication.registerUser(fullName, username, password);
            }
            catch (Authentication.UserAlreadyExistsException _) {
                throw new InvalidRequestException("Username already taken", HttpServletResponse.SC_BAD_REQUEST);
            }
            catch (Authentication.UserCreationFailureException _) {
                throw new InvalidRequestException("Failed to create user", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            String finalResponse = createJwtResponse(Authentication.generateToken(tenativeNewUser)).toString();
            try {
                resp.getWriter().println(finalResponse);
            } catch (IOException e) {
                throw new InvalidRequestException("Failed to write response", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        catch (InvalidRequestException e) {
            resp.setStatus(e.getStatus());
            try {
                resp.getWriter().println(new ObjectMapper().createObjectNode().put("error", e.getError()));
            }
            catch (IOException _) {
                // we cannot do anything here
            }
        }
    }
}
