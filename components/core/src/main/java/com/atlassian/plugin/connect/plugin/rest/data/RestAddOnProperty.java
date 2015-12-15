package com.atlassian.plugin.connect.plugin.rest.data;

import javax.annotation.concurrent.Immutable;

import com.atlassian.fugue.Either;
import com.atlassian.plugin.connect.api.plugin.property.AddOnProperty;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * This class represents an add-on property
 */
@JsonSerialize()
@Immutable
public class RestAddOnProperty
{
    @JsonProperty
    private final String key;

    @JsonSerialize(using = RestAddonPropertyValueSerializer.class)
    @JsonProperty
    private final Either<String, JsonNode> value;

    @JsonProperty
    private final String self;

    public RestAddOnProperty(@JsonProperty ("key") final String key, @JsonProperty ("value") final JsonNode value, @JsonProperty ("self") final String self)
    {
        this(key, value, self, true);
    }

    public RestAddOnProperty(final String key, final JsonNode value, final String self, final boolean unstringified)
    {
        this.key = key;
        this.value = unstringified ? Either.right(value) : Either.left(value.toString());
        this.self = self;
    }

    public static RestAddOnProperty valueOf(final AddOnProperty addOnProperty, final String baseURL, final boolean unstringified)
    {
        return new RestAddOnProperty(addOnProperty.getKey(), addOnProperty.getValue(), propertySelf(baseURL, addOnProperty.getKey()), unstringified);
    }

    public static String propertySelf(String baseURL, String propertyKey)
    {
        return baseURL + "/" + propertyKey;
    }
}
