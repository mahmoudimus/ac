package com.atlassian.plugin.connect.plugin.rest.data;

import javax.annotation.concurrent.Immutable;

import com.atlassian.fugue.Either;
import com.atlassian.plugin.connect.api.property.AddonProperty;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * This class represents an add-on property
 */
@JsonSerialize()
@Immutable
public class RestAddonProperty {
    @JsonProperty
    private final String key;

    @JsonSerialize(using = RestAddonPropertyValueSerializer.class)
    @JsonProperty
    private final Either<String, JsonNode> value;

    @JsonProperty
    private final String self;

    public RestAddonProperty(@JsonProperty("key") final String key, @JsonProperty("value") final JsonNode value, @JsonProperty("self") final String self) {
        this(key, value, self, true);
    }

    public RestAddonProperty(final String key, final JsonNode value, final String self, final boolean unstringified) {
        this.key = key;
        this.value = unstringified ? Either.right(value) : Either.left(value.toString());
        this.self = self;
    }

    public static RestAddonProperty valueOf(final AddonProperty addonProperty, final String baseURL, final boolean unstringified) {
        return new RestAddonProperty(addonProperty.getKey(), addonProperty.getValue(), propertySelf(baseURL, addonProperty.getKey()), unstringified);
    }

    public static String propertySelf(String baseURL, String propertyKey) {
        return baseURL + "/" + propertyKey;
    }
}
