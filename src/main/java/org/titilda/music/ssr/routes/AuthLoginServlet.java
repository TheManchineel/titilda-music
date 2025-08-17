package org.titilda.music.ssr.routes;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.controller.Authentication;
import org.titilda.music.base.model.User;
import org.titilda.music.ssr.BaseServlet;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

@WebServlet(urlPatterns = {"/auth/login"})
public class AuthLoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        if (username == null || password == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Authentication.validateCredentials(username, password)
                .ifPresentOrElse(
                        user -> {
                            Date now = new Date();
                            Cookie tokenCookie = new Cookie(
                                    BaseServlet.AUTHENTICATION_COOKIE_NAME,
                                    Authentication.generateToken(
                                            user,
                                            now,
                                            // TODO: make this configurable
                                            new Date(now.getTime() + 1000L * 3600 * 24 * 30) // 30 days
                                    )
                            );
                            tokenCookie.setHttpOnly(true);
                            tokenCookie.setPath("/");

                            resp.addCookie(tokenCookie);
                            try {
                                resp.sendRedirect("/home");
                            } catch (IOException e) {
                                System.out.println("Error sending redirect response: " + e.getMessage());
                            }
                        },
                        () -> {
                            try {
                                resp.sendRedirect("/login?error=invalid_credentials");
                            } catch (IOException e) {
                                System.out.println("Error sending error response: " + e.getMessage());
                            }
                        });
    }
}
