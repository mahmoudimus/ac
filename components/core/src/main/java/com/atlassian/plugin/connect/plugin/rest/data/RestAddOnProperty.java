package com.atlassian.plugin.connect.plugin.rest.data;

import java.io.IOException;

import com.atlassian.plugin.connect.plugin.property.AddOnProperty;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * This class represents an add-on property
 */
@Immutable
public class RestAddOnProperty
{
    @JsonProperty
    private final String key;
    @JsonProperty
    private final JsonNode value;
    @JsonProperty
    private final String self;

    public RestAddOnProperty(@JsonProperty ("key") final String key, @JsonProperty ("value") final JsonNode value, @JsonProperty ("self") final String self)
    {
        this.key = key;
        this.value = value;
        this.self = self;
    }

    public static RestAddOnProperty valueOf(final AddOnProperty addOnProperty, final String baseURL)
    {
        final Optional<JsonNode> potentialValue = parseJson(addOnProperty.getValue());
        Preconditions.checkState(potentialValue.isPresent(), "The value for the property " + addOnProperty.getKey() + " was not valid JSON.");
        return new RestAddOnProperty(addOnProperty.getKey(), potentialValue.get(), propertySelf(baseURL, addOnProperty.getKey()));
    }

    private static Optional<JsonNode> parseJson(String value) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj;
        try
        {
            actualObj = mapper.readTree(value);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return Optional.absent();
        }
        return Optional.of(actualObj);
    }

    public static String propertySelf(String baseURL, String propertyKey)
    {
        return baseURL + "/" + propertyKey;
    }
}
