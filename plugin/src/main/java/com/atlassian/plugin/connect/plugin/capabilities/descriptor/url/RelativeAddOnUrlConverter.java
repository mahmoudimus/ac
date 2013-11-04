package com.atlassian.plugin.connect.plugin.capabilities.descriptor.url;

import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ObjectArrays;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

/**
 * Converts an addon url that was specified as being relative to the addon's baseUrl to a local atlassian-connect
 * servlet url. This should be used everywhere relative-to-local addon url conversion is needed.
 */
@Component
public class RelativeAddOnUrlConverter
{
    private static String CONNECT_SERVLET_PREFIX = "/ac/";

    private final UrlVariableSubstitutor urlVariableSubstitutor;

    @Autowired
    public RelativeAddOnUrlConverter(UrlVariableSubstitutor urlVariableSubstitutor)
    {
        this.urlVariableSubstitutor = urlVariableSubstitutor;
    }

    /**
     * Converts an addon-relative url to a local servlet url using the plugin and module keys
     * and copies any query parameters from the relative url to the local url. This method also
     * accepts "extra" {@link NameValuePair}s to be added to the query string.
     *
     * @param pluginKey   the plugin key of the connect addon
     * @param addOnUrl    an addon-relative url
     * @param extraParams additional parameters to append to the url
     * @return the local servlet url corresponding to the module
     */
    public RelativeAddOnUrl addOnUrlToLocalServletUrl(String pluginKey, String addOnUrl, NameValuePair... extraParams)
    {
        String addonPath = (addOnUrl.startsWith("/") ? addOnUrl : "/" + addOnUrl);
        return new RelativeAddOnUrl(CONNECT_SERVLET_PREFIX + pluginKey + addonPath);
    }

    /**
     * @see #addOnUrlToLocalServletUrl(String, String, java.util.Map)
     */
    public RelativeAddOnUrl addOnUrlToLocalServletUrl(String pluginKey, String addOnUrl, Map<String, String> extraParams)
    {
        Collection<NameValuePair> nvpCollection = Collections2.transform(extraParams.entrySet(), new Function<Map.Entry<String, String>, NameValuePair>()
        {
            @Override
            public NameValuePair apply(Map.Entry<String, String> input)
            {
                return new BasicNameValuePair(input.getKey(), input.getValue());
            }
        });

        NameValuePair[] nvps = nvpCollection.toArray(ObjectArrays.newArray(NameValuePair.class, nvpCollection.size()));

        return addOnUrlToLocalServletUrl(pluginKey, addOnUrl, nvps);
    }

}
