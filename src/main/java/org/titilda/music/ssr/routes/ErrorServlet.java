package org.titilda.music.ssr.routes;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.exceptions.UnauthenticatedException;
import org.titilda.music.ssr.BaseGetServlet;

import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns={"/error"})
public class ErrorServlet extends BaseGetServlet {
    @Override
    protected String getTemplatePath() {
        return "error";
    }

    @Override
    protected Map<String, Object> prepareTemplateVariables(HttpServletRequest request, HttpServletResponse response) throws UnauthenticatedException {
        String errorMessage = switch (request.getParameter("error")) {
            case "bad_request" -> {
                response.setStatus(400);
                yield "Bad request.";
            }
            case "not_found" -> {
                response.setStatus(404);
                yield "Page not found.";
            }
            case "internal_server_error" -> {
                response.setStatus(500);
                yield "Internal server error.";
            }
            case null, default -> "Something went wrong.";
        };

        HashMap<String, Object> map = new HashMap<>();
        map.put("errorMessage", errorMessage);
        return map;
    }
}
