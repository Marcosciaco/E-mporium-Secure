package it.unibz.gangOf3.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BodyParser {

    public static JsonNode parseBody(String body) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(body);
        } catch (Exception e) {
            return null;
        }
    }

}
