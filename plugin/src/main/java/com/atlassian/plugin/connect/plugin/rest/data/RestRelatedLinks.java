package com.atlassian.plugin.connect.plugin.rest.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Collection;
import java.util.Map;

/**
 * Represents multiple collections of links where links in each collection are related by some concept.
 *
 * @since 1.1.0
 */
@JsonSerialize
public class RestRelatedLinks extends RestMapEntity
{
    public static final String RELATIONSHIP_SELF = "self";

    private RestRelatedLinks(Map<String, Collection<RestNamedLink>> relations)
    {
        putAll(relations);
    }

    public static class Builder
    {
        private Map<String, Collection<RestNamedLink>> relatedLinks = Maps.newHashMap();

        public Builder addRelatedLinks(String relationship, Collection<RestNamedLink> links)
        {
            getExisting(relationship).addAll(links);
            return this;
        }

        public Builder addRelatedLink(String relationship, RestNamedLink link)
        {
            getExisting(relationship).add(link);
            return this;
        }

        public RestRelatedLinks build()
        {
            return new RestRelatedLinks(relatedLinks);
        }

        private Collection<RestNamedLink> getExisting(String relationship)
        {
            Collection<RestNamedLink> existing = relatedLinks.get(relationship);
            if (existing == null)
            {
                existing = Sets.newHashSet();
                relatedLinks.put(relationship, existing);
            }
            return existing;
        }
    }
}
