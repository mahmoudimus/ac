package com.atlassian.plugin.connect.core.rest.data;

import org.codehaus.jackson.annotate.JsonProperty;

public class RestAddon extends RestLimitedAddon
{
    @JsonProperty
    private final RestHost host;

    @JsonProperty
    private final RestAddonLicense license;

    @JsonProperty
    private final RestRelatedLinks links;

    public RestAddon(@JsonProperty("key") final String key,
                     @JsonProperty("version") final String version,
                     @JsonProperty("state") final String state,
                     @JsonProperty("host") final RestHost host,
                     @JsonProperty("license") final RestAddonLicense license,
                     @JsonProperty("links") final RestRelatedLinks links)
    {
        super(key, version, state);
        this.host = host;
        this.license = license;
        this.links = links;
    }

    public RestAddonLicense getLicense()
    {
        return license;
    }

    public RestRelatedLinks getLinks()
    {
        return links;
    }

    public RestHost getHost()
    {
        return host;
    }
}
