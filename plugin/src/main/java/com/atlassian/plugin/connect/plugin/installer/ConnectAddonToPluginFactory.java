package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.BeanToModuleRegistrar;
import com.atlassian.plugin.connect.plugin.iframe.servlet.ConnectIFrameServlet;
import com.google.common.base.Strings;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class ConnectAddonToPluginFactory
{
    public static final String ATLASSIAN_CONNECT_INFO_PARAM = "atlassian-connect-addon";
    public static final String ATLASSIAN_LICENSING_ENABLED = "atlassian-licensing-enabled";
    public static final String CONFIGURE_URL = "configure.url";
    
    private final BeanToModuleRegistrar beanToModuleRegistrar;

    @Inject
    public ConnectAddonToPluginFactory(BeanToModuleRegistrar beanToModuleRegistrar)
    {
        this.beanToModuleRegistrar = beanToModuleRegistrar;
    }

    public Plugin create(ConnectAddonBean addon)
    {
        return create(addon,PluginState.DISABLED);
    }

    public Plugin create(ConnectAddonBean addon, PluginState state)
    {
        ConnectAddonPlugin plugin = new ConnectAddonPlugin(beanToModuleRegistrar.getRegisteredDescriptorsForAddon(addon.getKey()));
        plugin.setKey(addon.getKey());
        plugin.setName(addon.getName());
        plugin.setEnabledByDefault(true);
        plugin.setPluginsVersion(3);
        plugin.setPluginState(state);
        plugin.setPluginInformation(createPluginInfo(addon));
        
        return plugin;
    }

    private PluginInformation createPluginInfo(ConnectAddonBean addon)
    {
        PluginInformation pluginInfo = new PluginInformation();
        pluginInfo.setDescription(addon.getDescription());
        pluginInfo.setVendorName(addon.getVendor().getName());
        pluginInfo.setVendorUrl(addon.getVendor().getUrl());
        pluginInfo.setVersion(addon.getVersion());


        pluginInfo.addParameter(ATLASSIAN_CONNECT_INFO_PARAM, "true");

        if (addon.getEnableLicensing())
        {
            pluginInfo.addParameter(ATLASSIAN_LICENSING_ENABLED, "true");
        }

        ConnectPageModuleBean configurePage = addon.getModules().getConfigurePage();
        if (null != configurePage && !Strings.isNullOrEmpty(configurePage.getUrl()))
        {
            pluginInfo.addParameter(CONFIGURE_URL, ConnectIFrameServlet.iFrameServletPath(addon.getKey(), configurePage.getRawKey()));
        }

        return pluginInfo;
    }
}
