package org.titilda.music.ssr.routes;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.ssr.BaseGetServlet;

import java.io.IOException;
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
                case "invalid_data":
                    variables.put("error", "Please fill in all required fields.");
                    break;
                case "failed_to_register":
                    variables.put("error", "Registration failed. Please try again.");
                    break;
                case "user_already_exists":
                    variables.put("error", "Username already exists. Please choose a different username.");
                    break;
                default:
                    try {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown error: " + error);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
            }
        }
        return variables;
    }
}
