package com.atlassian.plugin.connect.plugin.module.page;

import java.util.Map;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.web.Condition;

/**
 * Container for all information required by invocations of {@link com.atlassian.plugin.connect.plugin.module.confluence.SpaceAdminIFrameAction}.
 */
public class SpaceAdminTabContext extends PageInfo
{
    private Plugin plugin;
    private String url;
    private String webItemKey;

    public SpaceAdminTabContext(Plugin plugin, String url, String webItemKey, String decorator, String templateSuffix, String title, Condition condition, Map<String, String> metaTagsContent)
    {
        super(decorator, templateSuffix, title, condition, metaTagsContent);

        this.plugin = plugin;
        this.url = url;
        this.webItemKey = webItemKey;
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
}
