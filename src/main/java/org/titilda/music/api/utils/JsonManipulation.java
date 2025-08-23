package org.titilda.music.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.titilda.music.api.AuthenticatedJsonRESTServlet;

public final class JsonManipulation {
    private JsonManipulation() {}

    public static String ensureStringField(JsonNode node, String fieldName) throws AuthenticatedJsonRESTServlet.InvalidRequestException {
        if (node.hasNonNull(fieldName)) {
            if (!node.get(fieldName).isTextual()) {
                throw new AuthenticatedJsonRESTServlet.InvalidRequestException("Field " + fieldName + " must be a string", 400);
            }
            return node.get(fieldName).asText();
        } else {
            throw new AuthenticatedJsonRESTServlet.InvalidRequestException("Missing field: " + fieldName, 400);
        }
    }

    public static JsonNode createJwtResponse (String jwt) {
        return new ObjectMapper().createObjectNode().put("access_token", jwt).put("token_type", "Bearer");
    }
}
