package com.atlassian.plugin.connect.plugin.lifecycle.upm;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.api.web.iframe.ConnectIFrameServletPath;
import com.atlassian.plugin.connect.modules.beans.ConfigurePageModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.PostInstallPageModuleMeta;
import com.atlassian.plugin.connect.plugin.descriptor.event.EventPublishingModuleValidationExceptionHandler;
import com.atlassian.plugin.connect.plugin.lifecycle.BeanToModuleRegistrar;
import com.google.common.base.Strings;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Named
public class ConnectAddonToPluginFactory
{

    private static final String PARAM_ATLASSIAN_CONNECT_INFO = "atlassian-connect-addon";
    private static final String PARAM_ATLASSIAN_LICENSING_ENABLED = "atlassian-licensing-enabled";
    private static final String PARAM_CONFIGURE_URL = "configure.url";
    private static final String PARAM_POST_INSTALL_URL = "post.install.url";
    
    private final BeanToModuleRegistrar beanToModuleRegistrar;
    private Consumer<Exception> moduleValidationExceptionHandler;

    @Inject
    public ConnectAddonToPluginFactory(BeanToModuleRegistrar beanToModuleRegistrar,
            EventPublishingModuleValidationExceptionHandler moduleValidationExceptionHandler)
    {
        this.beanToModuleRegistrar = beanToModuleRegistrar;
        this.moduleValidationExceptionHandler = moduleValidationExceptionHandler;
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

        pluginInfo.addParameter(PARAM_ATLASSIAN_CONNECT_INFO, "true");

        if (addon.getEnableLicensing())
        {
            pluginInfo.addParameter(PARAM_ATLASSIAN_LICENSING_ENABLED, "true");
        }

        addPluginInfoParameterForPageIfDeclared(pluginInfo, PARAM_CONFIGURE_URL, addon, new ConfigurePageModuleMeta().getDescriptorKey());
        addPluginInfoParameterForPageIfDeclared(pluginInfo, PARAM_POST_INSTALL_URL, addon, new PostInstallPageModuleMeta().getDescriptorKey());

        return pluginInfo;
    }

    private void addPluginInfoParameterForPageIfDeclared(PluginInformation pluginInfo, String parameterKey, ConnectAddonBean addon, String moduleType)
    {
        Optional<List<ModuleBean>> optionalPages = addon.getModules().getValidModuleListOfType(
                moduleType, moduleValidationExceptionHandler);
        optionalPages.ifPresent(new Consumer<List<ModuleBean>>()
        {

            @Override
            public void accept(List<ModuleBean> moduleBeans)
            {
                ConnectPageModuleBean page = (ConnectPageModuleBean) moduleBeans.get(0);
                if (null != page && !Strings.isNullOrEmpty(page.getUrl()))
                {
                    pluginInfo.addParameter(parameterKey, ConnectIFrameServletPath.forModule(addon.getKey(), page.getRawKey()));
                }
            }
        });
    }
}
