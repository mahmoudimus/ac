package com.atlassian.plugin.connect.plugin.web.condition;

abstract public class AddonConditionEvent
{
    private final String addonKey;

    private final String urlPath;

    private final long elapsedMillisecs;

    public AddonConditionEvent(String addonKey, String urlPath, long elapsedMillisecs)
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
