package org.titilda.music.api.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.controller.Authentication;

import java.io.IOException;

@WebServlet(urlPatterns = {"/api/auth/login"}) // Expecting POST /api/auth/login with JSON body {"username": "...", "password": "..."}
public class PostAuthLoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonNode node = new ObjectMapper().readTree(req.getInputStream());
        if ("application/json".equals(req.getContentType()) && node.has("username") && node.has("password")) {
            String username = node.get("username").asText();
            String password = node.get("password").asText();
            if (username.isEmpty() || password.isEmpty()) {
                resp.setStatus(400);
                resp.getWriter().println(new ObjectMapper().createObjectNode().put("error", "Username and password must not be empty").toString());
                return;
            }
            Authentication
                    .validateCredentials(username, password)
                    .map(Authentication::generateToken)
                    .ifPresentOrElse(
                            token -> {
                                try {
                                    resp.getWriter().println(new ObjectMapper().createObjectNode().put("access_token", token).put("token_type", "Bearer"));
                                } catch (IOException _) {
                                    // we cannot do anything here
                                }
                            },
                            () -> {
                                try {
                                    resp.setStatus(401);
                                    resp.getWriter().println(new ObjectMapper().createObjectNode().put("error", "Invalid credentials"));
                                } catch (IOException _) {
                                    // we cannot do anything here
                                }
                            }
                    );
        }
        else {
            resp.setStatus(400);
            resp.getWriter().println(new ObjectMapper().createObjectNode().put("error", "Username and password must not be empty").toString());
        }
    }
}
