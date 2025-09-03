package org.titilda.music.ssr.forms;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.controller.Authentication;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.database.DatabaseManager;
import org.titilda.music.base.model.User;
import org.titilda.music.ssr.BaseAuthenticatedPostWithRedirectServlet;
import org.titilda.music.ssr.BasePostWithRedirectServlet;
import org.titilda.music.ssr.CookieTokenInterface;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

@WebServlet(urlPatterns = { "/auth/logout-everywhere" })
public final class AuthLogoutEverywhereServlet extends BaseAuthenticatedPostWithRedirectServlet implements CookieTokenInterface {
    @Override
    protected String processRequestAndRedirect(HttpServletRequest request, HttpServletResponse response, User user) {
        try {
            Authentication.invalidateAllSessions(user);
        } catch (Authentication.FailedToInvalidateSessionException _) {
            return "/error?error=internal_server_error";
        }
        Cookie cookie = generateTokenCookie("");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "/login";
    }
}
