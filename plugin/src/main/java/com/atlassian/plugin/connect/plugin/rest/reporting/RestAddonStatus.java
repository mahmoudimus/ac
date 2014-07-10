package com.atlassian.plugin.connect.plugin.rest.reporting;

import org.codehaus.jackson.annotate.JsonProperty;

public class RestAddonStatus extends RestAddon
{
    @JsonProperty
    private final String state;

    @JsonProperty
    private final String license;

    public RestAddonStatus(@JsonProperty("key") final String key,
                           @JsonProperty("state") final String state,
                           @JsonProperty("version") final String version)
    {
        this(key, state, version, null);
    }

    public RestAddonStatus(@JsonProperty("key") final String key,
                           @JsonProperty("state") final String state,
                           @JsonProperty("version") final String version,
                           @JsonProperty("license") final String license)
    {
        super(key, version);
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
