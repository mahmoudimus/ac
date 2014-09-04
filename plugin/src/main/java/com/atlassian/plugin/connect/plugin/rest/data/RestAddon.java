package com.atlassian.plugin.connect.plugin.rest.data;

import org.codehaus.jackson.annotate.JsonProperty;
import com.atlassian.plugins.rest.common.Link;


public class RestAddon extends RestMinimalAddon
{
    @JsonProperty
    private final String state;

    @JsonProperty
    private final String license;

    @JsonProperty
    private final AddonApplink applink;

    @JsonProperty
    private final RestRelatedLinks links;

    public RestAddon(@JsonProperty ("key") final String key,
            @JsonProperty ("version") final String version,
            @JsonProperty ("type") final RestAddonType type,
            @JsonProperty ("state") final String state,
            @JsonProperty ("license") final String license,
            @JsonProperty ("applink") final AddonApplink applink,
            @JsonProperty ("links") final RestRelatedLinks links)
    {
        super(key, version, type);
        this.state = state;
        this.license = license;
        this.applink = applink;
        this.links = links;
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

    public RestRelatedLinks getLinks()
    {
        return links;
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
