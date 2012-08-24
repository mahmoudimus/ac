package com.atlassian.labs.remoteapps.spi.module;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.webresource.WebResourceManager;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 8/24/12 Time: 1:06 PM To change this template use
 * File | Settings | File Templates.
 */
public class RemoteCondition implements Condition
{

    private String url;
    private String pluginKey;
    private final WebResourceManager webResourceManager;

    public RemoteCondition(WebResourceManager webResourceManager)
    {
        this.webResourceManager = webResourceManager;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        url = params.get("url");
        pluginKey = params.get("pluginKey");
        checkNotNull(url);
        checkNotNull(pluginKey);
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        webResourceManager.requireResource("com.atlassian.labs.remoteapps-plugin:remote-condition");
        return true;
    }
}
