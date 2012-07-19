package com.atlassian.labs.remoteapps.services;

import com.atlassian.labs.remoteapps.api.services.PluginSettingsAsyncFactory;
import com.atlassian.labs.remoteapps.api.services.impl.DefaultPluginSettingsAsyncFactory;
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
