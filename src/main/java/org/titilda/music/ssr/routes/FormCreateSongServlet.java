package org.titilda.music.ssr.routes;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.controller.AssetCrudManager;
import org.titilda.music.base.model.User;
import org.titilda.music.base.model.forms.CreateSongFormData;
import org.titilda.music.base.util.MultiPartValidator;
import org.titilda.music.ssr.BaseAuthenticatedPostWithRedirectServlet;

@WebServlet(urlPatterns = { "/form/create-song" })
@MultipartConfig
public final class FormCreateSongServlet extends BaseAuthenticatedPostWithRedirectServlet {

    @Override
    protected String processRequestAndRedirect(HttpServletRequest request, HttpServletResponse response, User user) {
        if (!request.getContentType().startsWith("multipart/form-data;")) {
            return "/home?error=invalid_data";
        }

        try {
            CreateSongFormData formData = new CreateSongFormData(request);
            AssetCrudManager.createSongFromFormData(formData, user);
        }
        catch (MultiPartValidator.InvalidFormDataException _) {
            return "/home?error=invalid_data";
        }
        catch (MultiPartValidator.InvalidFieldDataException e) {
            return "/home?error=invalid_data&field=" + e.getField().getName();
        }

        catch (MultiPartValidator.InvalidDataSizeException e) {
            return "/home?error=invalid_data&field=" + e.getField().getName() + "&maxSize=" + e.getMaxSize();
        }

        return "/home";
    }
}
