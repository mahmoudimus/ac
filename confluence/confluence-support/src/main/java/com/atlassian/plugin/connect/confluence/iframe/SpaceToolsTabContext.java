package com.atlassian.plugin.connect.confluence.iframe;

/**
 * Container for all information required by invocations of {@link SpaceToolsIFrameAction}.
 */
public class SpaceToolsTabContext
{
    private final String addOnKey;
    private final String moduleKey;
    private final String displayName;
    private final String spaceAdminWebItemKey;

    public SpaceToolsTabContext(final String addOnKey, final String moduleKey, final String displayName,
            final String spaceAdminWebItemKey)
    {
        this.addOnKey = addOnKey;
        this.moduleKey = moduleKey;
        this.displayName = displayName;
        this.spaceAdminWebItemKey = spaceAdminWebItemKey;
    }

    public String getAddOnKey()
    {
        return addOnKey;
    }

    public String getModuleKey()
    {
        return moduleKey;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getSpaceAdminWebItemKey()
    {
        return spaceAdminWebItemKey;
    }

}
