package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor;

import com.google.common.base.Optional;

import java.util.Map;

/**
 * <p>Implementations of this interface are supposed to extract the parameters from web-panel's context to parameters which
 * will be included in URL of remote-web-panel iframe. </p>
 * <p> E.g., in order to include a page_id in a web-panel's iframe URL, we return a pair of ("page_id", page_id).</p>
 * <p>Components implementing this interface should be product specific.</p>
 */
public interface WebPanelParameterExtractor
{
    /**
     * Extracts the parameters from context.
     *
     * @param context a web panel's context.
     * @return a defined option containing pair of key,value which will be included in iframe's URL, otherwise none.
     */
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
