package org.titilda.music.ssr;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.exceptions.UnauthenticatedException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract base servlet that provides Thymeleaf template rendering
 * capabilities.
 * Subclasses need to implement getTemplatePath() and optionally override
 * prepareTemplateVariables() to provide template-specific data.
 */
public abstract sealed class BaseServlet extends HttpServlet permits BaseAuthenticatedServlet, BaseGetServlet {
    public static final String AUTHENTICATION_COOKIE_NAME = "titilda_music_login_token";

    /**
     * Returns the template name (without .html extension) to be rendered.
     * For example: "home", "user-profile", "error"
     * 
     * @return the template name
     */
    protected abstract String getTemplatePath();

    /**
     * Prepares variables to be passed to the template.
     * Override this method in subclasses to provide template-specific data.
     * 
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return a map of variables to pass to the template
     */
    abstract protected Map<String, Object> prepareTemplateVariables(HttpServletRequest request, HttpServletResponse response) throws UnauthenticatedException;

    /**
     * Renders the template and writes it to the response.
     * This method handles the common logic of template rendering.
     */
    protected final void renderTemplate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            response.setContentType("text/html;charset=UTF-8");
            Map<String, Object> variables;
            try {
                 variables = prepareTemplateVariables(request, response);
            }
            catch (UnauthenticatedException e) {
                response.sendRedirect("/login");
                return;
            }

            // Render template
            String html = TemplateManager.render(getTemplatePath(), variables);

            // Write response
            PrintWriter out = response.getWriter();
            out.print(html);
            out.flush();

        } catch (Exception e) {
            throw new ServletException("Uncaught error rendering template: " + getTemplatePath(), e);
        }
    }

    // UTILITY METHODS:

    /**
     * Extracts the value of the auth cookie from the given HTTP request.
     * If the cookie is not present, an empty Optional is returned.
     *
     * @param request the HttpServletRequest from which to extract the cookie
     * @return an Optional containing the value of the authentication token cookie,
     *         or an empty Optional if the cookie is not present
     */
    protected static Optional<String> getToken(HttpServletRequest request) {
        return Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[] {}))
            .filter(cookie -> cookie.getName().equals(AUTHENTICATION_COOKIE_NAME))
            .findFirst()
            .map(Cookie::getValue);
    }
}
