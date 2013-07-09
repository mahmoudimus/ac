package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor;

import com.google.common.base.Optional;

import java.util.Map;

/**
 * An object that maps context parameters to WebPanel parameters.
 */
public interface WebPanelParameterExtractor
{
    Optional<Map.Entry<String, String[]>> extract(final Map<String, Object> context);

    class ImmutableWebPanelParameterPair implements Map.Entry<String, String[]>
    {
        private final String key;
        private final String[] values;

        public ImmutableWebPanelParameterPair(final String key, final String[] values)
        {
            this.key = key;
            this.values = values;
        }

        @Override
        public String getKey()
        {
            return key;
        }

        @Override
        public String[] getValue()
        {
            return values;
        }

        @Override
        public String[] setValue(final String[] value)
        {
            throw new UnsupportedOperationException("This object is immutable");
        }
    }
}
