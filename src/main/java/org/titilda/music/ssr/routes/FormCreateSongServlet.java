package org.titilda.music.ssr.routes;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.titilda.music.base.model.User;
import org.titilda.music.base.util.MultiPartValidator;
import org.titilda.music.ssr.BaseAuthenticatedPostWithRedirectServlet;

import java.io.IOException;

@WebServlet(urlPatterns = { "/form/create-song" })
@MultipartConfig
public class FormCreateSongServlet extends BaseAuthenticatedPostWithRedirectServlet {
    private static class CreateSongFormData extends MultiPartValidator {
        @RequiredField(name="songName", contentTypes={})
        public String songName;

        @RequiredField(name="artist", contentTypes={})
        public String artist;

        @RequiredField(name="albumName", contentTypes={})
        public String albumName;

        @RequiredField(name="albumYear", contentTypes={})
        public Integer albumYear;

        @OptionalField(name="artwork", contentTypes={"image/jpeg", "image/png", "image/webp"})
        public Part artwork;

        @RequiredField(name="songFile", contentTypes={"audio/mpeg", "audio/flac"})
        public Part songFile;

        public CreateSongFormData(HttpServletRequest request) {
            super(request);
        }
    }

    @Override
    protected String processRequestAndRedirect(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        if (!request.getContentType().startsWith("multipart/form-data;")) {
            System.out.println(request.getContentType());
            return "/home?error=invalid_data";
        }

        try {
            CreateSongFormData formData = new CreateSongFormData(request);
            // TODO: implement song creation
            System.out.println("Form data:\n" + formData.songName + " (" + formData.songFile.getSize() + " bytes) - TO BE IMPLEMENTED");
        }
        catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return "/home?error=invalid_data";
        }

        return "/home";
    }
}
