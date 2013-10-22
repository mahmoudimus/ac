package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlBean
{
    private String url;

    public UrlBean(String url)
    {
        this.url = url;
    }

    public String getUrl()
    {
        return url;
    }

    public boolean hasUrl()
    {
        return null != url;
    }

    public URI createUri() throws URISyntaxException
    {
        return null == url ? null : new URI(url);
    }

}
