package org.titilda.music.ssr;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base servlet that provides Thymeleaf template rendering
 * capabilities.
 * Subclasses need to implement getTemplatePath() and optionally override
 * prepareTemplateVariables() to provide template-specific data.
 */
public abstract class BaseServlet extends HttpServlet {

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
    protected Map<String, Object> prepareTemplateVariables(HttpServletRequest request, HttpServletResponse response) {
        return new HashMap<>();
    }

    /**
     * Renders the template and writes it to the response.
     * This method handles the common logic of template rendering.
     */
    protected void renderTemplate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Set response content type
            response.setContentType("text/html;charset=UTF-8");

            // Get template variables
            Map<String, Object> variables = prepareTemplateVariables(request, response);

            // Render template
            String html = TemplateManager.render(getTemplatePath(), variables);

            // Write response
            PrintWriter out = response.getWriter();
            out.print(html);
            out.flush();

        } catch (Exception e) {
            throw new ServletException("Error rendering template: " + getTemplatePath(), e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        renderTemplate(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        renderTemplate(req, resp);
    }
}
