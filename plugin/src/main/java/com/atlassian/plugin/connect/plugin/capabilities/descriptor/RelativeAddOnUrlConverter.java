package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ObjectArrays;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Converts an addOn url that was specified as being relative to the addOn's baseUrl to a local atlassian-connect servlet url.
 * This should be used everywhere relative to local addOn url conversion is needed.
 */
@Component
public class RelativeAddOnUrlConverter
{
    public static String CONNECT_SERVLET_PREFIX = "/plugins/servlet/ac/";
    private final UrlVariableSubstitutor urlVariableSubstitutor;

    @Autowired
    public RelativeAddOnUrlConverter(UrlVariableSubstitutor urlVariableSubstitutor)
    {
        this.urlVariableSubstitutor = urlVariableSubstitutor;
    }

    /**
     * Converts an addOn-relative url to a local servlet url using the plugin and module keys
     * and copies any query parameters from the relative url to the local url. This method also
     * accepts "extra" {@link NameValuePair}s to be added to the query string
     * 
     * @param pluginKey
     * @param moduleKey
     * @param addOnUrl
     * @param extraParams
     * @return
     */
    public String addOnUrlToLocalServletUrl(String pluginKey, String addOnUrl, NameValuePair... extraParams)
    {
        String addonPath = (addOnUrl.startsWith("/") ? addOnUrl : "/" + addOnUrl);
        String addonPathMinusQueryString = StringUtils.split(addonPath,"?")[0];
        
        UriBuilder uriBuilder = new UriBuilder(Uri.parse(CONNECT_SERVLET_PREFIX + pluginKey + addonPathMinusQueryString));

        Map<String,String> contextParams = urlVariableSubstitutor.getContextVariableMap(addOnUrl);

        for (Map.Entry<String,String> entry : contextParams.entrySet())
        {
            uriBuilder.addQueryParameter(entry.getKey(),entry.getValue());
        }
        
        for(NameValuePair nvp : extraParams)
        {
            uriBuilder.addQueryParameter(nvp.getName(),nvp.getValue());
        }

        return uriBuilder.toString();
    }

    /**
     * Converts an addOn-relative url to a local servlet url using the plugin and module keys
     * and copies any query parameters from the relative url to the local url. This method also
     * accepts "extra" parameters to be added to the query string as a {@link Map}
     *
     * @param pluginKey
     * @param moduleKey
     * @param addOnUrl
     * @param extraParams
     * @return
     */
    public String addOnUrlToLocalServletUrl(String pluginKey, String addOnUrl, Map<String, String> extraParams)
    {
        Collection<NameValuePair> nvpCollection = Collections2.transform(extraParams.entrySet(), new Function<Map.Entry<String, String>, NameValuePair>()
        {
            @Override
            public NameValuePair apply(@Nullable Map.Entry<String, String> input)
            {
                return new BasicNameValuePair(input.getKey(), input.getValue());
            }
        });

        NameValuePair[] nvps = nvpCollection.toArray(ObjectArrays.newArray(NameValuePair.class,nvpCollection.size()));
        
        return addOnUrlToLocalServletUrl(pluginKey, addOnUrl, nvps);
    }
}
