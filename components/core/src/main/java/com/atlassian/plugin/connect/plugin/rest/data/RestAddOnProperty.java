package com.atlassian.plugin.connect.plugin.rest.data;

import javax.annotation.concurrent.Immutable;

import com.atlassian.plugin.connect.plugin.property.AddOnProperty;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * This class represents an add-on property
 */
@Immutable
public class RestAddOnProperty
{
    @JsonProperty
    private final String key;

    /**
     * The value property is in the process of being converted from a String data type to a Json node. This will be
     * done across two deprecation periods:
     *
     *  Stage 1 (current): Add jsonValue to the response, everybody switches over to use that instead.
     *  Stage 2: Once six months have passed then change the type of 'value' to be JsonNode as well.
     *  Stage 3: Once another six months have passed delete jsonValue entirely.
     */
    @JsonProperty
    private final String value;

    /**
     * See the comment on {@link #value}, this field will eventually be deleted.
     */
    @JsonProperty
    private final JsonNode jsonValue;

    @JsonProperty
    private final String self;

    public RestAddOnProperty(@JsonProperty ("key") final String key, @JsonProperty ("value") final JsonNode value, @JsonProperty ("self") final String self)
    {
        this.key = key;
        this.jsonValue = value;
        this.value = value.toString();
        this.self = self;
    }

    public static RestAddOnProperty valueOf(final AddOnProperty addOnProperty, final String baseURL)
    {
        return new RestAddOnProperty(addOnProperty.getKey(), addOnProperty.getValue(), propertySelf(baseURL, addOnProperty.getKey()));
    }

    public static String propertySelf(String baseURL, String propertyKey)
    {
        return baseURL + "/" + propertyKey;
    }
}
