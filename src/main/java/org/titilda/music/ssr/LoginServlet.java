package org.titilda.music.ssr;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

@WebServlet(urlPatterns = { "/login" })
public class LoginServlet extends BaseGetServlet {
    @Override
    protected String getTemplatePath() {
        return "login";
    }

    @Override
    protected Map<String, Object> prepareTemplateVariables(HttpServletRequest request, HttpServletResponse response) {
        return Map.of();
    }
}
