package com.atlassian.plugin.connect.plugin.property;

import java.io.IOException;
import java.util.Optional;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;


/**
 * This class contains common JSON methods.
 */
public class JsonCommon
{
    private JsonCommon() {}

    public static Optional<JsonNode> parseStringToJson(String rawJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode parsedJson;
        try
        {
            parsedJson = objectMapper.readTree(rawJson);
        }
        catch (IOException e)
        {
            return Optional.empty();
        }
        return Optional.of(parsedJson);
    }
}
