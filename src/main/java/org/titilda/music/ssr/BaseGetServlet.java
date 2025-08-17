package org.titilda.music.ssr;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        renderTemplate(req, resp);
    }
}
