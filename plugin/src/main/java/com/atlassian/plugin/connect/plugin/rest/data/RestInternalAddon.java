package com.atlassian.plugin.connect.plugin.rest.data;

import com.atlassian.plugins.rest.common.Link;
import org.codehaus.jackson.annotate.JsonProperty;

public class RestInternalAddon extends RestAddon
{
    @JsonProperty
    private final AddonApplink applink;

    public RestInternalAddon(@JsonProperty("key") String key,
                             @JsonProperty("version") String version,
                             @JsonProperty("state") String state,
                             @JsonProperty("license") RestAddonLicense license,
                             @JsonProperty("links") RestRelatedLinks links,
                             @JsonProperty("applink") AddonApplink applink)
    {
        super(key, version, state, license, links);
        this.applink = applink;
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
