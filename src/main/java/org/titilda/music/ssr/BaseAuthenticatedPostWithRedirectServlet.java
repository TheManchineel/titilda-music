package org.titilda.music.ssr;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.controller.Authentication;
import org.titilda.music.base.model.User;

import java.io.IOException;

public abstract class BaseAuthenticatedPostWithRedirectServlet extends BasePostWithRedirectServlet {
    protected abstract String processRequestAndRedirect(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException;

    @Override
    protected final String processRequestAndRedirect(HttpServletRequest request, HttpServletResponse response) {
        return getToken(request)
                .flatMap(Authentication::validateToken)
                .map(user -> {
                    try {
                        return processRequestAndRedirect(request, response, user);
                    }
                    catch (ServletException | IOException _) {
                        return null;
                    }
                })
                .orElse("/login");
    }
}
