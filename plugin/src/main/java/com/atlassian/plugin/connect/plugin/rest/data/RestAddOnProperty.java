package com.atlassian.plugin.connect.plugin.rest.data;

import com.atlassian.plugin.connect.plugin.property.AddOnProperty;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.concurrent.Immutable;

/**
 * This class represents an add-on property
 *
 * @since TODO: fill in the proper version before merge
 */
@Immutable
public class RestAddOnProperty
{
    @JsonProperty
    private final String key;
    @JsonProperty
    private final String value;
    @JsonProperty
    private final String self;

    public RestAddOnProperty(@JsonProperty ("key") final String key, @JsonProperty ("value") final String value, @JsonProperty ("self") final String self)
    {
        this.key = key;
        this.value = value;
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
