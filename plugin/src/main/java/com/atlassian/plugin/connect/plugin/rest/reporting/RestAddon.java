package com.atlassian.plugin.connect.plugin.rest.reporting;

import org.codehaus.jackson.annotate.JsonProperty;
import com.atlassian.plugins.rest.common.Link;


public class RestAddon extends MinimalRestAddon
{
    @JsonProperty
    private final String state;

    @JsonProperty
    private final String license;

    @JsonProperty
    private final AddonApplink applink;

    public RestAddon(@JsonProperty ("key") final String key,
            @JsonProperty ("version") final String version,
            @JsonProperty ("type") final RestAddonType type,
            @JsonProperty ("state") final String state,
            @JsonProperty ("license") final String license,
            @JsonProperty ("applink") final AddonApplink applink)
    {
        super(key, version, type);
        this.state = state;
        this.license = license;
        this.applink = applink;
    }

    public String getState()
    {
        return state;
    }

    public String getLicense()
    {
        return license;
    }

    public AddonApplink getApplink()
    {
        return applink;
    }

    public static class AddonApplink
    {
        @JsonProperty
        private final String id;

        @JsonProperty
        private final Link self;

        public AddonApplink(@JsonProperty("id") final String id,
                @JsonProperty("self") final Link self)
        {
            this.id = id;
            this.self = self;
        }

        public String getId()
        {
            return id;
        }

        public Link getSelf()
        {
            return self;
        }
    }
}
