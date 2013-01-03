package com.atlassian.plugin.remotable.plugin.module;

import com.atlassian.plugin.remotable.spi.module.UserIsLoggedInCondition;
import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.impl.AbstractDelegatingPlugin;
import com.atlassian.plugin.web.Condition;

import java.util.Set;

/**
* Plugin that can load conditions from the remotable plugins plugin
*/
class ConditionLoadingPlugin extends AbstractDelegatingPlugin implements AutowireCapablePlugin
{
    private final AutowireCapablePlugin remotablePlugin;
    private final Set<Class<?>> productConditions;

    public ConditionLoadingPlugin(AutowireCapablePlugin remotablePlugin, Plugin delegate, Set<Class<?>> productConditions)
    {
        super(delegate);
        this.remotablePlugin = remotablePlugin;
        this.productConditions = productConditions;
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
        if (clazz.getPackage().equals(UserIsLoggedInCondition.class.getPackage()) ||
                productConditions.contains(clazz))
        {
            return remotablePlugin.autowire(clazz);
        }

        return ((AutowireCapablePlugin)getDelegate()).autowire(clazz);
    }

    @Override
    public <T> T autowire(Class<T> clazz,
            AutowireStrategy autowireStrategy) throws
            UnsupportedOperationException
    {
        if (clazz.getPackage().equals(UserIsLoggedInCondition.class.getPackage())||
                        productConditions.contains(clazz))
        {
            return remotablePlugin.autowire(clazz, autowireStrategy);
        }
        return ((AutowireCapablePlugin)getDelegate()).autowire(clazz,
                autowireStrategy);
    }

    @Override
    public void autowire(Object instance)
    {
        ((AutowireCapablePlugin)getDelegate()).autowire(instance);
    }

    @Override
    public void autowire(Object instance, AutowireStrategy autowireStrategy)
    {
        ((AutowireCapablePlugin)getDelegate()).autowire(instance, autowireStrategy);
    }
}
