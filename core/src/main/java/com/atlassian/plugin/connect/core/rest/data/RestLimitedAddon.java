package com.atlassian.plugin.connect.core.rest.data;

import org.codehaus.jackson.annotate.JsonProperty;

public class RestLimitedAddon extends RestMinimalAddon
{
    @JsonProperty
    private final String state;

    public RestLimitedAddon(@JsonProperty("key") String key,
                            @JsonProperty("version") String version,
                            @JsonProperty("state") final String state)
    {
        super(key, version);
        this.state = state;
    }

    public String getState()
    {
        return state;
    }
}
