package com.atlassian.plugin.connect.plugin.rest.reporting;

import org.codehaus.jackson.annotate.JsonProperty;

public class RestAddon
{
    @JsonProperty
    private final String key;

    @JsonProperty
    private final String state;

    @JsonProperty
    private final String version;

    @JsonProperty
    private final String license;

    public RestAddon(@JsonProperty("key") final String key,
                     @JsonProperty("state") final String state,
                     @JsonProperty("version") final String version,
                     @JsonProperty("license") final String license)
    {
        this.key = key;
        this.state = state;
        this.version = version;
        this.license = license;
    }

    public String getKey()
    {
        return key;
    }

    public String getState()
    {
        return state;
    }

    public String getVersion()
    {
        return version;
    }

    public String getLicense()
    {
        return license;
    }
}
