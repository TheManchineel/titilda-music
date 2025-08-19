package org.titilda.music.ssr.routes;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.ssr.BasePostWithRedirectServlet;
import org.titilda.music.ssr.BaseServlet;

import java.io.IOException;

@WebServlet(urlPatterns = { "/auth/logout" })
public final class AuthLogoutServlet extends BasePostWithRedirectServlet {
    @Override
    protected String processRequestAndRedirect(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Clear the authentication cookie
        Cookie cookie = generateTokenCookie("");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return "/login";
    }
}
