package org.titilda.music.ssr;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.controller.Authentication;
import org.titilda.music.base.exceptions.UnauthenticatedException;
import org.titilda.music.base.model.User;

import java.io.IOException;
import java.util.Map;

public abstract class BaseAuthenticatedGetServlet extends BaseGetServlet {
    abstract protected Map<String, Object> prepareTemplateVariables(HttpServletRequest request, HttpServletResponse response, User user);

    @Override
    final protected Map<String, Object> prepareTemplateVariables(HttpServletRequest request, HttpServletResponse response) throws UnauthenticatedException {
        return getToken(request)
                .flatMap(Authentication::validateToken)
                .map(user -> prepareTemplateVariables(request, response, user))
                .orElseThrow(UnauthenticatedException::new);
    }
}
