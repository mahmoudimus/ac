package com.atlassian.plugin.connect.jira.field.option;

import java.io.IOException;
import java.util.Optional;

import com.google.gson.Gson;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Static utility methods for dealing with JSON values.
 */
public final class Json
{
    private static final Gson gson = new Gson();

    private Json() {}

    /**
     * Parses a rawJsonString into a {@link JsonNode}.
     *
     * @param rawJson verbatim JSON string
     * @return parsed JSON if successful, empty otherwise
     */
    public static Optional<JsonNode> parse(String rawJson)
    {
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

    /**
     * Convert JSON represented as a Java object into a {@link JsonNode}
     *
     * @param object may be String, Number, Map, Boolean
     * @return a JsonNode
     *
     */
    public static JsonNode toJsonNode(Object object)
    {
        return parse(gson.toJson(object)).get();
    }
}
