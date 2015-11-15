package com.atlassian.plugin.connect.plugin.util;

import java.io.IOException;

import com.atlassian.fugue.Option;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;


/**
 * This class contains common methods that are employed across the codebase.
 */
public class JsonCommon
{
    private JsonCommon() {}

    public static Option<JsonNode> parseStringToJson(String rawJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode parsedJson;
        try
        {
            parsedJson = objectMapper.readTree(rawJson);
        }
        catch (IOException e)
        {
            return Option.none();
        }
        return Option.some(parsedJson);
    }
}
