package org.titilda.music.base.controller;

import com.sksamuel.scrimage.ScaleMethod;
import com.sksamuel.scrimage.webp.WebpWriter;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.database.DatabaseManager;
import org.titilda.music.base.model.Song;
import org.titilda.music.base.model.User;
import org.titilda.music.base.model.forms.CreateSongFormData;
import org.titilda.music.base.model.mimetypes.SongMimeType;
import org.titilda.music.base.util.ConfigManager;

import com.sksamuel.scrimage.ImmutableImage;
import org.titilda.music.base.util.MultiPartValidator;

import javax.imageio.ImageWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public final class AssetCrudManager {
    private AssetCrudManager() {}

    private static final String SONG_SUBDIR = "songs";
    private static final String ARTWORK_SUBDIR = "artwork";

    public static final String assetPath = ConfigManager.getString(ConfigManager.ConfigKey.STATIC_ASSETS_ROOT);
    public static final String SONGS_PATH = assetPath + "/" + SONG_SUBDIR;
    public static final String ARTWORKS_PATH = assetPath + "/" + ARTWORK_SUBDIR;
    public static final int MAX_IMAGE_SIZE = 512;

    private static void processAndSaveImage(Path path, InputStream is) throws IOException {
        ImmutableImage image = ImmutableImage.loader().fromStream(is);
        if (image.width > MAX_IMAGE_SIZE || image.height > MAX_IMAGE_SIZE) {
            image = image.max(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE);
        }
        image.output(new WebpWriter(), path);
    }

    public static void createSongFromFormData(CreateSongFormData formData, User owner) throws MultiPartValidator.InvalidFormDataException {
        Path songPath = null;
        Path artworkPath = null;

        try (Connection con = DatabaseManager.getConnection(); InputStream is = formData.songFile.getInputStream()) {
            con.setAutoCommit(false);
            DAO dao = new DAO(con);
            SongMimeType songMimeType = SongMimeType.fromMimeType(formData.songFile.getContentType());
            UUID songId = UUID.randomUUID();

            songPath = Path.of(SONGS_PATH, songId.toString() + "." + songMimeType.getExtension());

            Files.copy(is, songPath);
            if (formData.artwork != null) {

                artworkPath = Path.of(ARTWORKS_PATH, songId + ".webp");
                try (InputStream artworkIs = formData.artwork.getInputStream()) {
                    processAndSaveImage(artworkPath, artworkIs);
                }
            }

            dao.insertSong(
                    new Song(
                            songId,
                            formData.songName,
                            formData.albumName,
                            formData.artist,
                            songMimeType.getMimeType(),
                            formData.albumYear,
                            formData.genre,
                            owner.getUsername()
                    )
            );

            con.commit();
        }
        catch (SQLException | IOException e) {
            // Roll-back operations to ensure fs is not in stale condition
            if (songPath != null) {
                try {
                    Files.deleteIfExists(songPath);
                }
                catch (Exception _) {}
            }

            if (artworkPath != null) {
                try {
                    Files.deleteIfExists(artworkPath);
                }
                catch (Exception _) {}
            }
            e.printStackTrace();
            throw new MultiPartValidator.InvalidFormDataException(e.getMessage());
        }
    }
}
