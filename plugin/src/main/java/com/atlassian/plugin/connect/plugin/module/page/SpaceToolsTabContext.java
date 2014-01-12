package com.atlassian.plugin.connect.plugin.module.page;

import com.atlassian.plugin.Plugin;

/**
 * Container for all information required by invocations of {@link com.atlassian.plugin.connect.plugin.module.confluence.SpaceToolsIFrameAction}.
 */
public class SpaceToolsTabContext
{
    private final Plugin plugin;
    private final String url;
    private final String spaceToolsWebItemKey;
    private final String spaceAdminWebItemKey;
    private final PageInfo pageInfo;

    public SpaceToolsTabContext(Plugin plugin, String url, String spaceToolsWebItemKey, String spaceAdminWebItemKey, PageInfo pageInfo)
    {
        this.plugin = plugin;
        this.url = url;
        this.spaceToolsWebItemKey = spaceToolsWebItemKey;
        this.spaceAdminWebItemKey = spaceAdminWebItemKey;
        this.pageInfo = pageInfo;
    }

    public String getSpaceToolsWebItemKey()
    {
        return spaceToolsWebItemKey;
    }

    public String getSpaceAdminWebItemKey() {
        return spaceAdminWebItemKey;
    }

    public Plugin getPlugin()
    {
        return plugin;
    }

    public String getUrl()
    {
        return url;
    }

    public PageInfo getPageInfo()
    {
        return pageInfo;
    }
}
