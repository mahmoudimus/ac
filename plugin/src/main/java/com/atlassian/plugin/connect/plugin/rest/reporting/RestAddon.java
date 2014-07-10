package com.atlassian.plugin.connect.plugin.rest.reporting;

import org.codehaus.jackson.annotate.JsonProperty;

public class RestAddon
{
    @JsonProperty
    private final String key;

    @JsonProperty
    private final String version;

    public RestAddon(@JsonProperty("key") final String key,
                     @JsonProperty("version") final String version)
    {
        this.key = key;
        this.version = version;
    }

    public String getKey()
    {
        return key;
    }

    public String getVersion()
    {
        return version;
    }
}
