package org.titilda.music.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public abstract class AuthenticatedJsonDeleteServlet extends AuthenticatedJsonRESTServlet {
    @Override
    protected final void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleHttpRequest(req, resp);
    }
}
