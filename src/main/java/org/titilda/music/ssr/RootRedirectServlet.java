package org.titilda.music.ssr;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name="/", urlPatterns = {"/"})
public final class RootRedirectServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (!req.getRequestURI().equals("/")) {
                resp.sendRedirect("/error?error=not_found");
                return;
            }

            if (getToken(req).isEmpty()) {
                resp.sendRedirect("/login");
                return;
            }

            resp.sendRedirect("/home");
        }
        catch (IOException _) {
            // there's nothing we can do here
        }
    }
}
