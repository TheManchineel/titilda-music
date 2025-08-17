package org.titilda.music.ssr;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.controller.Authentication;
import org.titilda.music.base.model.User;

import java.io.IOException;

public abstract class BaseAuthenticatedPostWithRedirectServlet extends BasePostWithRedirectServlet {
    abstract protected String processRequestAndRedirect(HttpServletRequest request, HttpServletResponse response, User user);

    @Override
    final protected String processRequestAndRedirect(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        return getToken(request)
                .flatMap(Authentication::validateToken)
                .map(user -> processRequestAndRedirect(request, response, user))
                .orElse("/login");

    }
}
