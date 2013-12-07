package com.atlassian.plugin.connect.plugin.module.page;

import com.atlassian.plugin.Plugin;

/**
 * Container for all information required by invocations of {@link com.atlassian.plugin.connect.plugin.module.confluence.SpaceToolsIFrameAction}.
 */
public class SpaceToolsTabContext
{
    private final Plugin plugin;
    private final String url;
    private final String webItemKey;
    private final PageInfo pageInfo;

    public SpaceToolsTabContext(Plugin plugin, String url, String webItemKey, PageInfo pageInfo)
    {
        this.plugin = plugin;
        this.url = url;
        this.webItemKey = webItemKey;
        this.pageInfo = pageInfo;
    }

    public String getWebItemKey()
    {
        return webItemKey;
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
