package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

public class UrlBean {

    private String url;

    public UrlBean(String url)
    {
        this.url = url;
    }

    public String getUrl()
    {
        return url;
    }

    // TODO: Factor out
    public boolean isAbsolute()
    {
        return (null != getUrl() && getUrl().startsWith("http"));
    }
}
