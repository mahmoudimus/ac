package com.atlassian.labs.remoteapps.plugin.service;

import com.atlassian.labs.remoteapps.api.service.PluginSettingsAsyncFactory;
import com.atlassian.labs.remoteapps.host.common.service.DefaultPluginSettingsAsyncFactory;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.springframework.beans.factory.FactoryBean;

public class PluginSettingsAsyncFactoryBean implements FactoryBean
{
    private final PluginSettingsAsyncFactory pluginSettingsFactory;

    public PluginSettingsAsyncFactoryBean(PluginSettingsFactory pluginSettingsFactory)
    {
        this.pluginSettingsFactory = new DefaultPluginSettingsAsyncFactory(pluginSettingsFactory);
    }

    @Override
    public Object getObject() throws Exception
    {
        return pluginSettingsFactory;
    }

    @Override
    public Class getObjectType()
    {
        return pluginSettingsFactory.getClass();
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }
}
