package com.atlassian.labs.remoteapps.modules;

import java.net.URI;

abstract class AbstractIFrameHost implements IFrameHost
{
    @Override
    public final String getUrl()
    {
        return getUrl(extractUrl());
    }

    abstract String extractUrl();

    private String getUrl(String url)
    {
        final URI hostUri = URI.create(url);
        return getUrl(hostUri.getScheme(), hostUri.getHost(), hostUri.getPort());
    }

    private String getUrl(String scheme, String host, int port)
    {
        final StringBuilder url = new StringBuilder().append(scheme).append("://").append(host);
        if (port > 0)
        {
            url.append(":").append(port);
        }
        return url.toString();
    }
}
