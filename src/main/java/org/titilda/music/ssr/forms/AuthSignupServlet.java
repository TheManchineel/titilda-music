package org.titilda.music.ssr.forms;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.controller.Authentication;
import org.titilda.music.ssr.BasePostWithRedirectServlet;

import java.io.IOException;

@WebServlet(urlPatterns = { "/auth/signup" })
public final class AuthSignupServlet extends BasePostWithRedirectServlet {
    @Override
    protected String processRequestAndRedirect(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fullName = request.getParameter("fullName");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (username == null || password == null || fullName == null) {
            return "/signup?error=invalid_data";
        }
        try {
            String newUserToken = Authentication.registerUser(fullName, username, password);
            response.addCookie(generateTokenCookie(newUserToken));
            return "/home";
        }
        catch (Authentication.UserCreationFailureException _) {
            return "/signup?error=failed_to_register";
        } catch (Authentication.UserAlreadyExistsException _) {
            return "/signup?error=user_already_exists";
        }
    }
}
