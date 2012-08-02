package com.atlassian.labs.remoteapps.apputils.spring;

import com.atlassian.labs.remoteapps.apputils.Environment;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Builds an environment for the target plugin
 */
public final class EnvironmentFactoryBean implements FactoryBean
{
    private final Environment environment;

    public EnvironmentFactoryBean(PluginRetrievalService pluginRetrievalService, PluginSettingsFactory pluginSettingsFactory)
    {
        environment = new EnvironmentImpl(pluginRetrievalService.getPlugin().getKey(), pluginSettingsFactory);
    }

    @Override
    public Object getObject() throws Exception
    {
        return environment;
    }

    @Override
    public Class getObjectType()
    {
        return Environment.class;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }
}
