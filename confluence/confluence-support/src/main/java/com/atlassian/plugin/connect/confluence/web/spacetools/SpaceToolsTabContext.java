package com.atlassian.plugin.connect.confluence.web.spacetools;

/**
 * Container for all information required by invocations of {@link SpaceToolsIFrameAction}.
 */
public class SpaceToolsTabContext {
    private final String addonKey;
    private final String moduleKey;
    private final String displayName;
    private final String spaceAdminWebItemKey;

    public SpaceToolsTabContext(final String addonKey, final String moduleKey, final String displayName,
                                final String spaceAdminWebItemKey) {
        this.addonKey = addonKey;
        this.moduleKey = moduleKey;
        this.displayName = displayName;
        this.spaceAdminWebItemKey = spaceAdminWebItemKey;
    }

    public String getAddonKey() {
        return addonKey;
    }

    public String getModuleKey() {
        return moduleKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSpaceAdminWebItemKey() {
        return spaceAdminWebItemKey;
    }

}
