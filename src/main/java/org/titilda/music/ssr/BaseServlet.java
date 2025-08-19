package org.titilda.music.ssr;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Optional;

/**
 * Abstract base servlet that provides Thymeleaf template rendering
 * capabilities.
 * Subclasses need to implement getTemplatePath() and optionally override
 * prepareTemplateVariables() to provide template-specific data.
 */
public abstract sealed class BaseServlet extends HttpServlet permits BaseGetServlet, BasePostWithRedirectServlet, RootRedirectServlet {
    public static final String AUTHENTICATION_COOKIE_NAME = "titilda_music_login_token";


    // UTILITY METHODS:

    /**
     * Extracts the value of the auth cookie from the given HTTP request.
     * If the cookie is not present, an empty Optional is returned.
     *
     * @param request the HttpServletRequest from which to extract the cookie
     * @return an Optional containing the value of the authentication token cookie,
     *         or an empty Optional if the cookie is not present
     */
    protected static Optional<String> getToken(HttpServletRequest request) {
        return Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[] {}))
            .filter(cookie -> cookie.getName().equals(AUTHENTICATION_COOKIE_NAME))
            .findFirst()
            .map(Cookie::getValue);
    }

    protected static Cookie generateTokenCookie(String token) {
        Cookie tokenCookie = new Cookie(
                BaseServlet.AUTHENTICATION_COOKIE_NAME,
                token
        );
        tokenCookie.setHttpOnly(true);
        tokenCookie.setPath("/");
        return tokenCookie;
    }
}
