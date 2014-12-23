package com.atlassian.plugin.connect.plugin.rest.data;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;
import javax.annotation.Nullable;

import static com.google.common.collect.ImmutableList.copyOf;

/**
 * Beans used in listing of properties for add-ons.
 *
 * @since TODO: fill in the proper version before merge
 */

public class RestAddOnPropertiesBean
{
    @JsonProperty(value="keys")
    private final List<RestAddOnPropertyBean> properties;

    public RestAddOnPropertiesBean(final Iterable<String> keys, final String baseURL)
    {
        properties = copyOf(Iterables.transform(keys, new Function<String, RestAddOnPropertyBean>()
        {
            @Override
            public RestAddOnPropertyBean apply(@Nullable final String key)
            {
                return new RestAddOnPropertyBean(baseURL + key, key);
            }
        }));
    }

    public static RestAddOnPropertiesBean valueOf(final Iterable<String> keys, final String baseURL)
    {
        return new RestAddOnPropertiesBean(keys, baseURL);
    }

    public static class RestAddOnPropertyBean
    {
        @JsonProperty
        private final String self;
        @JsonProperty
        private final String key;

        public RestAddOnPropertyBean(@JsonProperty final String self, @JsonProperty final String key)
        {
            this.self = self;
            this.key = key;
        }
    }
}
