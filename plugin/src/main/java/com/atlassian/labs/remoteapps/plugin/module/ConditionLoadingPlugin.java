package com.atlassian.labs.remoteapps.plugin.module;

import com.atlassian.labs.remoteapps.spi.module.UserIsLoggedInCondition;
import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.impl.AbstractDelegatingPlugin;

/**
* Plugin that can load conditions from the remote apps plugin
*/
class ConditionLoadingPlugin extends AbstractDelegatingPlugin
{
    private final AutowireCapablePlugin remoteAppsPlugin;

    public ConditionLoadingPlugin(AutowireCapablePlugin remoteAppsPlugin, Plugin delegate)
    {
        super(delegate);
        this.remoteAppsPlugin = remoteAppsPlugin;
    }

    @Override
    public <T> Class<T> loadClass(String clazz, Class<?> callingClass) throws ClassNotFoundException
    {
        try
        {
            return super.loadClass(clazz, callingClass);
        }
        catch (ClassNotFoundException ex)
        {
            return (Class<T>) getClass().getClassLoader().loadClass(clazz);
        }
    }

    @Override
    public <T> T autowire(Class<T> clazz) throws UnsupportedOperationException
    {
        if (clazz.getPackage().equals(UserIsLoggedInCondition.class.getPackage()))
        {
            return remoteAppsPlugin.autowire(clazz);
        }

        return super.autowire(clazz);
    }

    @Override
    public <T> T autowire(Class<T> clazz,
            AutowireStrategy autowireStrategy) throws
            UnsupportedOperationException
    {
        if (clazz.getPackage().equals(UserIsLoggedInCondition.class.getPackage()))
        {
            return remoteAppsPlugin.autowire(clazz, autowireStrategy);
        }
        return super.autowire(clazz,
                autowireStrategy);
    }
}
