package com.atlassian.plugin.connect.plugin.lifecycle.upm;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.api.iframe.servlet.ConnectIFrameServletPath;
import com.atlassian.plugin.connect.modules.beans.ConfigurePageModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.PostInstallPageModuleMeta;
import com.atlassian.plugin.connect.plugin.lifecycle.BeanToModuleRegistrar;
import com.google.common.base.Strings;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class ConnectAddonToPluginFactory
{
    public static final String ATLASSIAN_CONNECT_INFO_PARAM = "atlassian-connect-addon";
    public static final String ATLASSIAN_LICENSING_ENABLED = "atlassian-licensing-enabled";
    public static final String CONFIGURE_URL = "configure.url";
    public static final String POST_INSTALL_URL = "post.install.url";
    
    private final BeanToModuleRegistrar beanToModuleRegistrar;

    @Inject
    public ConnectAddonToPluginFactory(BeanToModuleRegistrar beanToModuleRegistrar)
    {
        this.beanToModuleRegistrar = beanToModuleRegistrar;
    }

    public Plugin create(ConnectAddonBean addon, PluginState state)
    {
        ConnectAddonPlugin plugin = new ConnectAddonPlugin(beanToModuleRegistrar.getRegisteredDescriptorsForAddon(addon.getKey()));
        plugin.setKey(addon.getKey());
        plugin.setName(addon.getName());
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

        addPluginInfoParameterForPageIfDeclared(pluginInfo, CONFIGURE_URL, addon, new ConfigurePageModuleMeta().getDescriptorKey());
        addPluginInfoParameterForPageIfDeclared(pluginInfo, POST_INSTALL_URL, addon, new PostInstallPageModuleMeta().getDescriptorKey());

        return pluginInfo;
    }

    private void addPluginInfoParameterForPageIfDeclared(PluginInformation pluginInfo, String parameterKey, ConnectAddonBean addon, String descriptorKey)
    {
        if (addon.getModules() != null && addon.getModules().get(descriptorKey) != null)
        {
            ConnectPageModuleBean page = (ConnectPageModuleBean) addon.getModules().get(descriptorKey).get(0);
            if (null != page && !Strings.isNullOrEmpty(page.getUrl()))
            {
                pluginInfo.addParameter(parameterKey, ConnectIFrameServletPath.forModule(addon.getKey(), page.getRawKey()));
            }
        }
    }
}