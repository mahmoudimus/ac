package com.atlassian.plugin.connect.jira.field.option;

import java.io.IOException;
import java.util.Optional;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Static utility methods for dealing with JSON values.
 */
public class Json
{
    private Json() {}

    public static Optional<JsonNode> parse(String rawJson) {
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
