package com.atlassian.plugin.connect.plugin.rest.data;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An optionally-named link relating to an entity.
 *
 * @since 1.1.0
 */
@JsonSerialize
public class RestNamedLink extends RestMapEntity
{

    public static final String ATTR_HREF = "href";
    public static final String ATTR_NAME = "name";

    public RestNamedLink(@Nonnull String href)
    {
        this(href, null);
    }

    public RestNamedLink(@Nonnull String href, @Nullable String name)
    {
        put(ATTR_HREF, href);
        putIfNotNull(ATTR_NAME, name);
    }

    public RestNamedLink(@Nonnull Map<String, Object> map)
    {
        putAll(map);
    }

    @SuppressWarnings ("unchecked")
    public static RestNamedLink valueOf(Object link)
    {
        if (link instanceof RestNamedLink)
        {
            return (RestNamedLink) link;
        }
        else if (link instanceof Map)
        {
            return new RestNamedLink((Map) link);
        }
        return null;
    }
}
