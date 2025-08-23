package org.titilda.music.base.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.database.DatabaseManager;
import org.titilda.music.base.model.Song;
import org.titilda.music.base.model.User;
import org.titilda.music.base.model.mimetypes.SongMimeType;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public abstract class StaticAssetsServletFoundation extends HttpServlet {

    public abstract Optional<String> getToken(HttpServletRequest req);
    protected abstract void sendNotFound(HttpServletResponse res) throws IOException;
    protected abstract void sendForbidden(HttpServletResponse res) throws IOException;

    private static class StaticAssetAccessNotAllowedException extends Exception {}
    private static class StaticAssetNotFoundException extends Exception {}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String subpath = req.getRequestURI().substring("/static/".length());
        String[] resComponents = subpath.split("/");
        try {
            if (resComponents.length != 2) {
                throw new StaticAssetNotFoundException();
            }


            String resType = resComponents[0];
            String[] resNameParts = resComponents[1].split("\\.");

            if (resNameParts.length != 2) {
                throw new StaticAssetNotFoundException();
            }
            String resName = resNameParts[0];

            UUID resId;
            try {
                resId = UUID.fromString(resName);
            } catch (IllegalArgumentException _) {
                throw new StaticAssetNotFoundException();
            }

            String resExt = resNameParts[1];
            try (Connection connection = DatabaseManager.getConnection()) {
                User user = getToken(req)
                        .flatMap(Authentication::validateToken)
                        .orElseThrow(StaticAssetAccessNotAllowedException::new);

                DAO dao = new DAO(connection);

                switch (resType) {
                    case "artworks" -> {
                        if (!resExt.equals("webp"))
                            throw new StaticAssetNotFoundException();
                        Song song = dao.getSongById(resId)
                                .filter(s -> s.getOwner().equals(user.getUsername()))
                                .orElseThrow(StaticAssetNotFoundException::new);
                        try {
                            AssetCrudManager.writeArtworkImageToStream(song, resp.getOutputStream());
                        }
                        catch (IOException _) {
                            throw new StaticAssetNotFoundException();
                        }
                    }
                    case "songs" -> {
                        SongMimeType mimeType;
                        try {
                            mimeType = SongMimeType.fromExtension(resExt);
                            Song song = dao.getSongById(resId)
                                    .filter(s -> s.getOwner().equals(user.getUsername()))
                                    .filter(s -> s.getAudioMimeType().equals(mimeType.getMimeType()))
                                    .orElseThrow(StaticAssetNotFoundException::new);
                            resp.setContentType(mimeType.getMimeType());
                            AssetCrudManager.writeSongToStream(song, resp.getOutputStream());
                        } catch (IllegalArgumentException _) {
                            throw new StaticAssetNotFoundException();
                        }
                    }
                    default -> throw new StaticAssetNotFoundException();
                }
            }
            catch (SQLException _) {
                sendNotFound(resp);
            }
        }
        catch (StaticAssetNotFoundException _) {
            try {
                sendNotFound(resp);
            }
            catch (IOException _) {
                // ignored
            }
        }
        catch (StaticAssetAccessNotAllowedException _) {
            try {
                sendForbidden(resp);
            }
            catch (IOException _) {
                // ignored
            }
        }
        catch (IOException _) {
            // ignored
        }
    }
}
