package com.atlassian.plugin.connect.plugin.installer;

import javax.inject.Named;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.plugin.iframe.servlet.ConnectIFrameServlet;

import com.google.common.base.Strings;

@Named
public class ConnectAddonToPluginFactory
{
    public static final String ATLASSIAN_CONNECT_INFO_PARAM = "atlassian-connect-addon";
    public static final String ATLASSIAN_LICENSING_ENABLED = "atlassian-licensing-enabled";
    public static final String CONFIGURE_URL = "configure.url";

    public Plugin create(ConnectAddonBean addon)
    {
        Plugin plugin  = new ConnectAddonPlugin();
        plugin.setKey(addon.getKey());
        plugin.setName(addon.getName());
        plugin.setPluginsVersion(3);
        plugin.setPluginInformation(createPluginInfo(addon));
        
        return plugin;
    }

    private PluginInformation createPluginInfo(ConnectAddonBean addon)
    {
        PluginInformation pluginInfo = createPluginInfo(addon);
        pluginInfo.setDescription(addon.getDescription());
        pluginInfo.setVendorName(addon.getVendor().getName());
        pluginInfo.setVendorUrl(addon.getVendor().getUrl());
        pluginInfo.setVersion(addon.getVersion());
        
        
        pluginInfo.getParameters().put(ATLASSIAN_CONNECT_INFO_PARAM,"true");
        
        if(addon.getEnableLicensing())
        {
            pluginInfo.getParameters().put(ATLASSIAN_LICENSING_ENABLED,"true");
        }

        ConnectPageModuleBean configurePage = addon.getModules().getConfigurePage();
        if(null != configurePage && !Strings.isNullOrEmpty(configurePage.getUrl()))
        {
            pluginInfo.getParameters().put(CONFIGURE_URL, ConnectIFrameServlet.iFrameServletPath(addon.getKey(), configurePage.getKey()));
        }
        
        return pluginInfo;
    }
}
