package org.titilda.music.ssr.routes;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.ssr.BaseGetServlet;

import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = { "/signup" })
public final class SignupServlet extends BaseGetServlet {
    @Override
    protected String getTemplatePath() {
        return "signup";
    }

    @Override
    protected Map<String, Object> prepareTemplateVariables(HttpServletRequest request, HttpServletResponse response) {
        HashMap<String, Object> variables = new HashMap<>();
        String error = request.getParameter("error");
        if (error != null) {
            switch (error) {
                case "invalid_data" -> variables.put("error", "Please fill in all required fields.");
                case "failed_to_register" -> variables.put("error", "Registration failed. Please try again.");
                case "user_already_exists" -> variables.put("error", "Username already exists. Please choose a different username.");
                default -> variables.put("error", "Something went wrong. Please try again.");
            }
        }
        return variables;
    }
}
