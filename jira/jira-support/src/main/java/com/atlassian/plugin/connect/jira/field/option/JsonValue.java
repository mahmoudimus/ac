package com.atlassian.plugin.connect.jira.field.option;

import java.util.Objects;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public final class JsonValue
{
    private final static Gson gson = new Gson();
    private final static JsonParser jsonParser = new JsonParser();

    private final String jsonRepresentation;

    /**
     * Wraps a verbatim JSON into this class
     * @param json json string
     * @return instance of JsonValue backed by the supplied JSON
     */
    public static Optional<JsonValue> parse(final String json)
    {
        try
        {
            return Optional.of(new JsonValue(gson.toJson(jsonParser.parse(json).getAsString())));
        }
        catch (JsonParseException jsonParseException)
        {
            return Optional.empty();
        }
    }

    public String toJson()
    {
        return jsonRepresentation;
    }

    private JsonValue(String jsonRepresentation)
    {
        this.jsonRepresentation = Preconditions.checkNotNull(jsonRepresentation);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        JsonValue that = (JsonValue) o;

        return Objects.equals(this.jsonRepresentation, that.jsonRepresentation);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(jsonRepresentation);
    }

    @Override
    public String toString()
    {
        return toJson();
    }
}
