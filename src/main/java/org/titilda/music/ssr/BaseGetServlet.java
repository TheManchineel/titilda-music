package org.titilda.music.ssr;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.titilda.music.base.exceptions.InternalErrorException;
import org.titilda.music.base.exceptions.UnauthenticatedException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public abstract non-sealed class BaseGetServlet extends BaseServlet {

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
    abstract protected Map<String, Object> prepareTemplateVariables(HttpServletRequest request, HttpServletResponse response) throws UnauthenticatedException, InternalErrorException;

    /**
     * Renders the template and writes it to the response.
     * This method handles the common logic of template rendering.
     */
    protected final void renderTemplate(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setContentType("text/html;charset=UTF-8");
            Map<String, Object> variables = prepareTemplateVariables(request, response);

            // Render template
            String html = TemplateManager.render(getTemplatePath(), variables);

            // Write response
            PrintWriter out = response.getWriter();
            out.print(html);
            out.flush();
        }
        catch (UnauthenticatedException _) {
            try {
                response.sendRedirect("/login");
            }
            catch (IOException e) {
                // we tried our best
            }
        }
        catch (IOException | InternalErrorException _) {
            try {
                response.sendRedirect("/error");
            }
            catch (IOException _) {
                // eff Murphy's law
            }
        }
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        renderTemplate(req, resp);
    }
}
