package org.titilda.music.base.model.forms;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.titilda.music.base.util.MultiPartValidator;

public class CreateSongFormData extends MultiPartValidator {
    @MultipartField(name = "songName", contentTypes = {}, maxSize = 256L)
    public String songName;

    @MultipartField(name = "artist", contentTypes = {}, maxSize = 256L)
    public String artist;

    @MultipartField(name = "albumName", contentTypes = {}, maxSize = 256L)
    public String albumName;

    @MultipartField(name="genre", contentTypes = {}, maxSize = 256L)
    public String genre;

    @MultipartField(name = "albumYear", contentTypes = {}, maxSize = 8L)
    public Integer albumYear;

    @MultipartField(name = "artwork", contentTypes = {"image/jpeg", "image/png", "image/webp"}, maxSize = 1024L * 1024L * 2L, required = false)
    // 4 MiB
    public Part artwork;

    // TODO: unfortunately I've found no way to grab the allowed MIME types from the appropriate enum :/
    @MultipartField(name = "songFile", contentTypes = {"audio/mpeg", "audio/wav", "audio/ogg", "audio/flac"}, maxSize = 1024L * 1024L * 50L)
    // 50 MiB
    public Part songFile;

    public CreateSongFormData(HttpServletRequest request) throws InvalidFormDataException, InvalidDataSizeException, InvalidFieldDataException {
        super(request);
    }
}
