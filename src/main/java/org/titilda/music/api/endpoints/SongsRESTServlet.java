package org.titilda.music.api.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.api.AuthenticatedJsonRESTServlet;
import org.titilda.music.base.controller.AssetCrudManager;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.model.Song;
import org.titilda.music.base.model.User;
import org.titilda.music.base.model.forms.CreateSongFormData;
import org.titilda.music.base.util.MultiPartValidator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(urlPatterns = {"/api/songs"})
public class SongsRESTServlet extends AuthenticatedJsonRESTServlet {
    // POST /api/songs
    @Override
    protected JsonNode processApiPost(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException {
        ensurePathComponentsFinished(getPathComponents(req));

        try {
            AssetCrudManager.createSongFromFormData(new CreateSongFormData(req), user);
        } catch (MultiPartValidator.InvalidFormDataException _) {
            throw new InvalidRequestException("Invalid data", HttpServletResponse.SC_BAD_REQUEST);
        } catch (MultiPartValidator.InvalidFieldDataException e) {
            throw new InvalidRequestException("Invalid data in field: " + e.getField().getName(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (MultiPartValidator.InvalidDataSizeException e) {
            throw new InvalidRequestException("Size of data in field " + e.getField().getName() + " exceeds " + e.getMaxSize(), HttpServletResponse.SC_BAD_REQUEST);
        }

        return new JsonMapper().createObjectNode().put("status", "ok");
    }

    @Override
    // GET /api/songs?excludePlaylist=UUID
    protected JsonNode processApiGet(User user, HttpServletRequest req, Connection dbConnection) throws InvalidRequestException, SQLException {
        ensurePathComponentsFinished(getPathComponents(req));
        UUID excludePlaylist = Optional.ofNullable(req.getParameter("excludePlaylist"))
                .map(s -> {
                    try {
                        return UUID.fromString(s);
                    } catch (IllegalArgumentException _) {
                            return null;
                    }
                })
                .orElse(null);

        DAO dao = new DAO(dbConnection);
        List<Song> songs = excludePlaylist == null ? dao.getSongsOfUser(user) : dao.getSongsNotInPlaylist(user, excludePlaylist);
        return new JsonMapper().valueToTree(songs);
    }
}
