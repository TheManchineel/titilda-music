package org.titilda.music.ssr.routes;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.controller.Authentication;
import org.titilda.music.ssr.BasePostWithRedirectServlet;

import java.io.IOException;

@WebServlet(urlPatterns = {"/auth/login"})
public final class AuthLoginServlet extends BasePostWithRedirectServlet {
    private static final String FAILURE_REDIRECT_URL = "/login?error=invalid_credentials";
    private static final String SUCCESS_REDIRECT_URL = "/home";

    @Override
    protected String processRequestAndRedirect(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        if (username == null || password == null) {
            return FAILURE_REDIRECT_URL;
        }
        return Authentication.validateCredentials(username, password)
                .map(
                        user -> {
                            resp.addCookie(generateTokenCookie(Authentication.generateToken(user)));
                            return SUCCESS_REDIRECT_URL;
                        }
                )
                .orElse(FAILURE_REDIRECT_URL);
    }
}
