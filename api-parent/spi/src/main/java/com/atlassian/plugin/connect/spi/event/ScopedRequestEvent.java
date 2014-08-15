package com.atlassian.plugin.connect.spi.event;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@PrivacyPolicySafe
public abstract class ScopedRequestEvent
{

    private static String URI_PATH_PREFIX = "rest/atlassian-connect/";

    @PrivacyPolicySafe
    private final String httpMethod;

    @PrivacyPolicySafe
    private final String httpRequestUri;

    public ScopedRequestEvent(String httpMethod, String httpRequestUri)
    {
        super();
        this.httpMethod = httpMethod;
        this.httpRequestUri = trimPath(httpRequestUri);
    }

    private static String trimPath(String uri)
    {
        String[] pathElems = StringUtils.substringAfter(uri, URI_PATH_PREFIX).split("/");
        if (pathElems.length > 1)
        {
            // version + first part of the path after it
            return pathElems[0] + "/" + pathElems[1];
        }
        else
        {
            return uri;
        }
    }

    public String getHttpMethod()
    {
        return httpMethod;
    }

    public String getHttpRequestUri()
    {
        return httpRequestUri;
    }
}
