package org.titilda.music.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public abstract class AuthenticatedJsonPostServlet extends AuthenticatedJsonRESTServlet {
    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleHttpRequest(req, resp);
    }
}
