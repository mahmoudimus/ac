package com.atlassian.plugin.connect.modules.beans.nested;

import com.google.common.base.Objects;

/**
 * Pojo to hold the body POST'ed to the addon's blueprint context url.
 */
public final class BlueprintContextPostBody
{
    private final String addonKey;
    private final String blueprintKey;
    private final String spaceKey;
    private final String userKey;

    public BlueprintContextPostBody(String addonKey, String blueprintKey, String spaceKey, String userKey)
    {
        this.addonKey = addonKey;
        this.blueprintKey = blueprintKey;
        this.spaceKey = spaceKey;
        this.userKey = userKey;
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        BlueprintContextPostBody body = (BlueprintContextPostBody) o;
        return Objects.equal(addonKey, body.addonKey) &&
               Objects.equal(blueprintKey, body.blueprintKey) &&
               Objects.equal(spaceKey, body.spaceKey) &&
               Objects.equal(userKey, body.userKey);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(addonKey, blueprintKey, spaceKey, userKey);
    }
}
