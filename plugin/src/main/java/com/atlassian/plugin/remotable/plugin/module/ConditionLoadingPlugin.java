package com.atlassian.plugin.remotable.plugin.module;

import com.atlassian.plugin.remotable.spi.module.UserIsLoggedInCondition;
import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.impl.AbstractDelegatingPlugin;

/**
* Plugin that can load conditions from the remotable plugins plugin
*/
class ConditionLoadingPlugin extends AbstractDelegatingPlugin
{
    private final AutowireCapablePlugin remotablePlugin;

    public ConditionLoadingPlugin(AutowireCapablePlugin remotablePlugin, Plugin delegate)
    {
        super(delegate);
        this.remotablePlugin = remotablePlugin;
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
            return remotablePlugin.autowire(clazz);
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
            return remotablePlugin.autowire(clazz, autowireStrategy);
        }
        return super.autowire(clazz,
                autowireStrategy);
    }
}
