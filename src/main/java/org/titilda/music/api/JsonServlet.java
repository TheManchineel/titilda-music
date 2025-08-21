package org.titilda.music.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet(urlPatterns = { "/api/status"})
public class JsonServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        String jsonResponse = mapper.createObjectNode().put("status", "up").toString();
        try {
            resp.getWriter().write(jsonResponse);
        }
        catch (IOException _) {
            System.out.println("Error writing JSON response to socket.");
        }
    }
}
