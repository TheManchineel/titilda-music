package org.titilda.music.ssr;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Example concrete implementation of BaseServlet that renders the home page.
 * Demonstrates how to use the abstract servlet pattern with Thymeleaf
 * templates.
 */
@WebServlet(urlPatterns = { "/home" })
public class HomeServlet extends BaseServlet {

    @Override
    protected String getTemplatePath() {
        return "home"; // This will resolve to templates/home.html
    }

    @Override
    protected Map<String, Object> prepareTemplateVariables(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> variables = new HashMap<>();

        // Add some example variables
        variables.put("message", "Welcome to TiTilda Music! 🎵");
        variables.put("currentTime", new java.util.Date());
        variables.put("userAgent", request.getHeader("User-Agent"));

        // You can add any data your template needs
        // For example: user info, music data, etc.

        return variables;
    }
}
