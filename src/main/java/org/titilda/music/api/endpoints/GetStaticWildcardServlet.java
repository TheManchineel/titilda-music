package org.titilda.music.api.endpoints;

import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.api.BearerTokenInterface;
import org.titilda.music.base.controller.StaticAssetsServletFoundation;

import java.io.IOException;
import java.util.Optional;

@WebServlet(urlPatterns = {"/static/*"})
public final class GetStaticWildcardServlet extends StaticAssetsServletFoundation implements BearerTokenInterface {
    @Override
    public Optional<String> getToken(HttpServletRequest req) {
        return BearerTokenInterface.super.getToken(req);
    }

    @Override
    protected void sendNotFound(HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.getWriter().println(new JsonMapper().createObjectNode().put("error", "Not found").toString());
    }

    @Override
    protected void sendForbidden(HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.getWriter().println(new JsonMapper().createObjectNode().put("error", "Forbidden").toString());
    }
}
