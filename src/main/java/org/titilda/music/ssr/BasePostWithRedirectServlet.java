package org.titilda.music.ssr;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public abstract non-sealed class BasePostWithRedirectServlet extends BaseServlet {
    /**
     * Processes an HTTP POST request and determines the URI to which the client should be redirected.
     * This method is abstract and must be implemented by servlets to implement the specific post logic required.
     *
     * @param request  the HttpServletRequest object containing the client's request
     * @param response the HttpServletResponse object for sending the response
     * @return the URI to redirect to as a String, or null if no redirection is required
     * @throws ServletException if an error occurs while processing the request
     * @throws IOException if an input or output error is detected when handling the request
     */
    abstract protected String processRequestAndRedirect(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String redirectUri = processRequestAndRedirect(req, resp);
            if (redirectUri != null) {
                resp.sendRedirect(redirectUri);
            }
        }
        catch (Exception e) {
            // TODO: use custom exceptions, better message passing & error reporting on exceptions
            throw new ServletException("Uncaught error processing request: " + req.getRequestURI(), e);
        }
    }
}
