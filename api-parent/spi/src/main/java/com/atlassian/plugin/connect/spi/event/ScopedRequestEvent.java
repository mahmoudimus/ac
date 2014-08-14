package com.atlassian.plugin.connect.spi.event;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;

@PrivacyPolicySafe
public abstract class ScopedRequestEvent
{

    private static final long TRIMPRECISION = 100;

    private static final long THRESHOLD = 20 * 1000;

    private static String URI_PATH_PREFIX = "rest/atlassian-connect/";

    @PrivacyPolicySafe
    private final String httpMethod;

    @PrivacyPolicySafe
    private final String httpRequestUri;

    @PrivacyPolicySafe
    private final int responseCode;

    @PrivacyPolicySafe
    private final long duration;

    private static String trimPath(String uri)
    {
        String[] pathElems = StringUtils.substringAfter(uri, URI_PATH_PREFIX).split("/");
        if(pathElems.length > 1)
        {
            //version + first part of the path after it
            return pathElems[0] + "/" + pathElems[1];
        }
        else
        {
            return uri;
        }
    }

    public ScopedRequestEvent(String httpMethod, String httpRequestUri, int responseCode, long duration)
    {
        super();
        this.httpMethod = httpMethod;
        this.httpRequestUri = trimPath(httpRequestUri);
        this.responseCode = responseCode;
        this.duration = duration > THRESHOLD ? -1 : duration / TRIMPRECISION;
    }

    public String getHttpMethod()
    {
        return httpMethod;
    }

    public String getHttpRequestUri()
    {
        return httpRequestUri;
    }

    public long getDuration()
    {
        return this.duration;
    }

    public int getResponseCode()
    {
        return this.responseCode;
    }
}
