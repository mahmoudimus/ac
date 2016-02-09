package com.atlassian.plugin.connect.modules.beans.nested;

import java.util.Locale;

/**
 * Pojo to hold the body POST'ed to the addon's blueprint context url.
 */
public final class BlueprintContextPostBody
{
    private final String addonKey;
    private final String blueprintKey;
    private final String spaceKey;
    private final String userKey;
    private final Locale userLocale;

    public BlueprintContextPostBody(String addonKey, String blueprintKey, String spaceKey, String userKey, Locale userLocale)
    {
        this.addonKey = addonKey;
        this.blueprintKey = blueprintKey;
        this.spaceKey = spaceKey;
        this.userKey = userKey;
        this.userLocale = userLocale;
    }

    public String getAddonKey()
    {
        return addonKey;
    }

    public String getBlueprintKey()
    {
        return blueprintKey;
    }

    public String getSpaceKey()
    {
        return spaceKey;
    }

    public String getUserKey()
    {
        return userKey;
    }

    public Locale getUserLocale()
    {
        return userLocale;
    }
}
