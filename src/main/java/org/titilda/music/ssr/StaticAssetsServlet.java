package org.titilda.music.ssr;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.controller.StaticAssetsServletFoundation;

import java.io.IOException;
import java.util.Optional;

@WebServlet(urlPatterns = {"/static/*"})
public final class StaticAssetsServlet extends StaticAssetsServletFoundation implements CookieTokenInterface {
    @Override
    public Optional<String> getToken(HttpServletRequest req) {
        return CookieTokenInterface.super.getToken(req);
    }

    @Override
    protected void sendNotFound(HttpServletResponse res) throws IOException {
        res.sendRedirect("/error?error=not_found");
    }

    @Override
    protected void sendForbidden(HttpServletResponse res) throws IOException {
        res.sendRedirect("/login");
    }
}
