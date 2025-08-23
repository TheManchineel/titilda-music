package org.titilda.music.ssr;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Optional;

public interface CookieTokenInterface {
    /**
     * Extracts the value of the auth cookie from the given HTTP request.
     * If the cookie is not present, an empty Optional is returned.
     *
     * @param request the HttpServletRequest from which to extract the cookie
     * @return an Optional containing the value of the authentication token cookie,
     * or an empty Optional if the cookie is not present
     */
     default Optional<String> getToken(HttpServletRequest request) {
        return Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[]{}))
                .filter(cookie -> cookie.getName().equals(BaseServlet.AUTHENTICATION_COOKIE_NAME))
                .findFirst()
                .map(Cookie::getValue);
    }
}
