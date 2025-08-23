package org.titilda.music.ssr.forms;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.database.DatabaseManager;
import org.titilda.music.ssr.BasePostWithRedirectServlet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

@WebServlet(urlPatterns = { "/auth/logout-everywhere" })
public final class AuthLogoutEverywhereServlet extends BasePostWithRedirectServlet {
    @Override
    protected String processRequestAndRedirect(HttpServletRequest request, HttpServletResponse response) {
        try (Connection connection = DatabaseManager.getConnection()) {
            DAO dao = new DAO(connection);
            Date now = new Date();
            now = new Date(now.getTime() / 1000L * 1000L);
            dao.setInvalidationDate(request.getParameter("username"), now);
            Cookie cookie = generateTokenCookie("");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            return "/login";
        }
        catch (SQLException _) {
            return "/error?error=internal_server_error";
        }
    }
}
