package com.atlassian.plugin.connect.plugin.rest.reporting;

import org.codehaus.jackson.annotate.JsonProperty;

public class RestAddonStatus extends RestAddon
{
    @JsonProperty
    private final String state;

    @JsonProperty
    private final String license;

    public RestAddonStatus(@JsonProperty ("key") final String key,
            @JsonProperty ("version") final String version,
            @JsonProperty ("type") final RestAddonType type,
            @JsonProperty ("state") final String state)
    {
        this(key, version, type, state, null);
    }

    public RestAddonStatus(@JsonProperty ("key") final String key,
            @JsonProperty ("version") final String version,
            @JsonProperty ("type") final RestAddonType type,
            @JsonProperty ("state") final String state,
            @JsonProperty ("license") final String license)
    {
        super(key, version, type);
        this.state = state;
        this.license = license;
    }

    public String getState()
    {
        return state;
    }

    public String getLicense()
    {
        return license;
    }
}
