package org.titilda.music.ssr;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.controller.Authentication;
import org.titilda.music.ssr.exceptions.InternalErrorException;
import org.titilda.music.ssr.exceptions.UnauthenticatedException;
import org.titilda.music.base.model.User;

import java.util.Map;

public abstract class BaseAuthenticatedGetServlet extends BaseGetServlet {
    protected abstract Map<String, Object> prepareTemplateVariables(HttpServletRequest request, HttpServletResponse response, User user) throws InternalErrorException;

    @Override
    protected final Map<String, Object> prepareTemplateVariables(HttpServletRequest request, HttpServletResponse response) throws UnauthenticatedException, InternalErrorException {
        User user = getToken(request)
                .flatMap(Authentication::validateToken)
                .orElseThrow(UnauthenticatedException::new);
        return prepareTemplateVariables(request, response, user);
    }
}
