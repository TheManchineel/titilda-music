package org.titilda.music.api;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public interface BearerTokenInterface {
    default Optional<String> getToken(HttpServletRequest request) {
        return Optional
                .ofNullable(request.getHeader("Authorization"))
                .filter(header -> header.toLowerCase().startsWith("bearer "))
                .map(header -> header.substring("Bearer ".length()));
    }
}
