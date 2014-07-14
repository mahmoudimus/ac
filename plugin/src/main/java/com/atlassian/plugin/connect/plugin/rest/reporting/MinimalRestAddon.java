package com.atlassian.plugin.connect.plugin.rest.reporting;

import org.codehaus.jackson.annotate.JsonProperty;

public class MinimalRestAddon
{
    @JsonProperty
    private final String key;

    @JsonProperty
    private final String version;

    @JsonProperty
    private final RestAddonType type;

    public MinimalRestAddon(@JsonProperty ("key") final String key,
            @JsonProperty ("version") final String version,
            @JsonProperty ("type") final RestAddonType type)
    {
        this.key = key;
        this.version = version;
        this.type = type;
    }

    public String getKey()
    {
        return key;
    }

    public String getVersion()
    {
        return version;
    }

    public RestAddonType getType()
    {
        return type;
    }

}
