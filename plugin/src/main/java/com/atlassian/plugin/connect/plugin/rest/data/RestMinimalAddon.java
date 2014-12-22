package com.atlassian.plugin.connect.plugin.rest.data;

import org.codehaus.jackson.annotate.JsonProperty;

public class RestMinimalAddon
{
    @JsonProperty
    private final String key;

    @JsonProperty
    private final String version;

    public RestMinimalAddon(@JsonProperty ("key") final String key,
            @JsonProperty ("version") final String version)
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
