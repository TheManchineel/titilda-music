package org.titilda.music.ssr;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.titilda.music.base.controller.StaticAssetsServletFoundation;

import java.util.Arrays;
import java.util.Optional;

/**
 * Abstract base servlet that provides Thymeleaf template rendering
 * capabilities.
 * Subclasses need to implement getTemplatePath() and optionally override
 * prepareTemplateVariables() to provide template-specific data.
 */
public abstract sealed class BaseServlet extends HttpServlet implements CookieTokenInterface permits BaseGetServlet, BasePostWithRedirectServlet, RootRedirectServlet {
    public static final String AUTHENTICATION_COOKIE_NAME = "titilda_music_login_token";

    protected static Cookie generateTokenCookie(String token) {
        Cookie tokenCookie = new Cookie(
                BaseServlet.AUTHENTICATION_COOKIE_NAME,
                token
        );
        tokenCookie.setMaxAge(60 * 60 * 24 * 30);
        tokenCookie.setHttpOnly(true);
        tokenCookie.setPath("/");
        return tokenCookie;
    }
}
