package com.atlassian.plugin.connect.plugin.rest.data;

import java.io.IOException;

import javax.annotation.concurrent.Immutable;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.property.AddOnProperty;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        return new RestAddOnProperty(addOnProperty.getKey(), addOnProperty.getValue(), propertySelf(baseURL, addOnProperty.getKey()));
    }

    public static String propertySelf(String baseURL, String propertyKey)
    {
        return baseURL + "/" + propertyKey;
    }
}
