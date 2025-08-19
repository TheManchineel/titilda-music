package org.titilda.music.ssr.routes;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.controller.Authentication;
import org.titilda.music.ssr.BaseGetServlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/login"})
public final class LoginServlet extends BaseGetServlet {
    @Override
    protected String getTemplatePath() {
        return "login";
    }

    @Override
    protected Map<String, Object> prepareTemplateVariables(HttpServletRequest request, HttpServletResponse response) {
        HashMap<String, Object> variables = new HashMap<>();
        String error = request.getParameter("error");
        if (error != null) {
            switch (error) {
                case "invalid_credentials" -> {
                    variables.put("error", "Invalid username or password.");
                }
                default -> {
                    try {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST , "Unknown error: " + error);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return variables;
    }
}
