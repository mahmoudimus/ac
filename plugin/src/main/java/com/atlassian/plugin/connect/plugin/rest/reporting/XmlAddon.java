package com.atlassian.plugin.connect.plugin.rest.reporting;

import org.codehaus.jackson.annotate.JsonProperty;

public class XmlAddon
{
    @JsonProperty
    private final String key;

    @JsonProperty
    private final String state;

    @JsonProperty
    private final String version;

    public XmlAddon(@JsonProperty("key") final String key,
                    @JsonProperty("state") final String state,
                    @JsonProperty("version") final String version)
    {
        this.key = key;
        this.state = state;
        this.version = version;
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
}
