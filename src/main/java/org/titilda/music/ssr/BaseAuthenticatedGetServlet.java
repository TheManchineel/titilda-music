package org.titilda.music.ssr;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public abstract non-sealed class BaseAuthenticatedGetServlet extends BaseAuthenticatedServlet {
    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        renderTemplate(req, resp);
    }
}
