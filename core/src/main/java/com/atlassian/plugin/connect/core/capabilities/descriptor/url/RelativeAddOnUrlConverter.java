package com.atlassian.plugin.connect.core.capabilities.descriptor.url;

import org.apache.http.NameValuePair;
import org.springframework.stereotype.Component;

/**
 * Converts an addon url that was specified as being relative to the addon's baseUrl to a local atlassian-connect
 * servlet url. This should be used everywhere relative-to-local addon url conversion is needed.
 */
@Component
public class RelativeAddOnUrlConverter
{
    private static String CONNECT_SERVLET_PREFIX = "/ac/";

    /**
     * Converts an addon-relative url to a local servlet url using the plugin and module keys
     * and copies any query parameters from the relative url to the local url. This method also
     * accepts "extra" {@link NameValuePair}s to be added to the query string.
     *
     * @param pluginKey   the plugin key of the connect addon
     * @param addOnUrl    an addon-relative url
     * @return the local servlet url corresponding to the module
     */
    public RelativeAddOnUrl addOnUrlToLocalServletUrl(String pluginKey, String addOnUrl)
    {
        String addonPath = (addOnUrl.startsWith("/") ? addOnUrl : "/" + addOnUrl);
        return new RelativeAddOnUrl(CONNECT_SERVLET_PREFIX + pluginKey + addonPath);
    }

}
