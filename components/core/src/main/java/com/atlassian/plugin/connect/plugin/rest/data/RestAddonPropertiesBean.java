package com.atlassian.plugin.connect.plugin.rest.data;

import com.google.common.collect.Iterables;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;

/**
 * Beans used in listing of properties for add-ons.
 */
public class RestAddonPropertiesBean {
    @JsonProperty(value = "keys")
    private final List<RestAddonPropertyBean> properties;

    public RestAddonPropertiesBean(final Iterable<String> keys, final String baseURL) {
        properties = copyOf(Iterables.transform(keys, key -> new RestAddonPropertyBean(baseURL + "/" + key, key)));
    }

    public static RestAddonPropertiesBean valueOf(final Iterable<String> keys, final String baseURL) {
        return new RestAddonPropertiesBean(keys, baseURL);
    }

    public static class RestAddonPropertyBean {
        @JsonProperty
        private final String self;
        @JsonProperty
        private final String key;

        public RestAddonPropertyBean(@JsonProperty final String self, @JsonProperty final String key) {
            this.self = self;
            this.key = key;
        }
    }
}
