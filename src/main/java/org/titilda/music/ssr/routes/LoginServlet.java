package org.titilda.music.ssr.routes;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.controller.Authentication;
import org.titilda.music.ssr.BaseGetServlet;

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
