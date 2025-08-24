package org.titilda.music.api;


import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;

@WebServlet(urlPatterns = {"/*"})
public final class FallbackRouterServlet extends HttpServlet {
    private static final HashSet<String> WHITELISTED_DEFAULT_PATHS =
            new HashSet<>(Arrays.asList(
                    "/css/home.css",
                    "/css/styles.css",
                    "/css/login.css",
                    "/css/playlist.css",
                    "/js/auth.js",
                    "/js/index.js",
                    "/js/playlist.js"
            ));

    private static void denyRequest(HttpServletResponse resp) {
        try {
            resp.setContentType("application/json");
            resp.getWriter().println(new JsonMapper().createObjectNode().put("error", "Resource not found").toString());
        } catch (IOException _) {
            // ignored
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String path = req.getPathInfo();
        ServletContext context = getServletContext();
        try {
            if (path.startsWith("/api/")) {
                denyRequest(resp);
            }
            else if (WHITELISTED_DEFAULT_PATHS.contains(path)) {
                RequestDispatcher defaultDispatcher = context.getNamedDispatcher("default");
                defaultDispatcher.forward(req, resp);
            }
            else {
                // serve index.html
                resp.setContentType("text/html");
                try (InputStream is = context.getResourceAsStream("/index.html")) {
                    is.transferTo(resp.getOutputStream());
                }
            }
        }
        catch (ServletException | IOException e) {
            System.out.println(e.getMessage());
        }
    }
    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) {
        denyRequest(resp);
    }
    @Override
    protected final void doPut(HttpServletRequest req, HttpServletResponse resp) {
        denyRequest(resp);
    }
    @Override
    protected final void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        denyRequest(resp);
    }
    @Override
    protected final void doPatch(HttpServletRequest req, HttpServletResponse resp) {
        denyRequest(resp);
    }
}
