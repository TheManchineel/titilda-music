package org.titilda.music.api.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.api.AuthenticatedJsonRESTServlet;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

@WebServlet(urlPatterns = {"/api/songs/*"})
public class SongsWildcardRESTServlet extends AuthenticatedJsonRESTServlet {
    @Override
    protected JsonNode processApiGet(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException, SQLException {
        Iterator<String> iter = getPathComponents(req);
        String songIdStr = getNextPathComponent(iter);
        ensurePathComponentsFinished(iter);
        DAO dao = new DAO(dbConnection);
        return (JsonNode) Optional.of(songIdStr)
                .map(s -> {
                    try {
                        return UUID.fromString(s);
                    }
                    catch (IllegalArgumentException _) {
                        return null;
                    }
                })
                .flatMap(dao::getSongById)
                .filter(song -> song.getOwner().equals(user.getUsername()))
                .map(new ObjectMapper()::valueToTree)
                .orElseThrow(() -> new InvalidRequestException("Song not found", HttpServletResponse.SC_NOT_FOUND));
    }
}
