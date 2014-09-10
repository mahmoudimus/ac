package com.atlassian.plugin.connect.spi.event;

abstract public class AddOnConditionEvent
{
    private final String addonKey;

    private final String urlPath;

    private final long elapsedMillisecs;

    public AddOnConditionEvent(String addonKey, String urlPath, long elapsedMillisecs)
    {
        this.elapsedMillisecs = elapsedMillisecs;
        this.urlPath = urlPath;
        this.addonKey = addonKey;
    }

    public String getAddonKey()
    {
        return addonKey;
    }

    public String getUrlPath()
    {
        return urlPath;
    }

    public long getElapsedMillisecs()
    {
        return elapsedMillisecs;
    }
}
