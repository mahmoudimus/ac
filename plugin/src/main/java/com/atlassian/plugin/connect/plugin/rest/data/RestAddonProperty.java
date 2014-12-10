package com.atlassian.plugin.connect.plugin.rest.data;

import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.service.AddOnPropertyService;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.3
 */
public class RestAddonProperty
{
    @JsonProperty
    private final String key;
    @JsonProperty
    private final String value;

    public RestAddonProperty(@JsonProperty ("key") final String key, @JsonProperty ("value") final String value)
    {
        this.key = key;
        this.value = value;
    }

    public static RestAddonProperty valueOf(final AddOnProperty addonProperty)
    {
        return new RestAddonProperty(addonProperty.getKey(), addonProperty.getValue());
    }
}
