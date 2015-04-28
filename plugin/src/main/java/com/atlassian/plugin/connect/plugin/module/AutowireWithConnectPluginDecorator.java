package com.atlassian.plugin.connect.plugin.module;

import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.impl.AbstractDelegatingPlugin;
import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;

/**
 * A decorator for addon bundles that delegates all autowiring requests to the actual connect plugin
 */
public class AutowireWithConnectPluginDecorator extends AbstractDelegatingPlugin implements AutowireCapablePlugin, ContainerManagedPlugin
{
    private final AutowireCapablePlugin theConnectPlugin;

    public AutowireWithConnectPluginDecorator(AutowireCapablePlugin theConnectPlugin, Plugin delegate)
    {
        super(delegate);
        this.theConnectPlugin = theConnectPlugin;
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
            return cast(getClass().getClassLoader().loadClass(clazz));
        }
    }

    @Override
    public <T> T autowire(Class<T> clazz) throws UnsupportedOperationException
    {
        return getContianerManagedPlugin().getContainerAccessor().createBean(clazz);
    }

    @Override
    public <T> T autowire(Class<T> clazz, AutowireStrategy autowireStrategy) throws UnsupportedOperationException
    {
        return getContianerManagedPlugin().getContainerAccessor().createBean(clazz);
    }

    @Override
    public void autowire(Object instance)
    {
        getContianerManagedPlugin().getContainerAccessor().injectBean(instance);
    }

    @Override
    public void autowire(Object instance, AutowireStrategy autowireStrategy)
    {
        getContianerManagedPlugin().getContainerAccessor().injectBean(instance);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> cast(Class<?> aClass)
    {
        return (Class<T>) aClass;
    }

    @Override
    public ContainerAccessor getContainerAccessor()
    {
        return getContianerManagedPlugin().getContainerAccessor();
    }

    @Override
    public PluginArtifact getPluginArtifact()
    {
        return getContianerManagedPlugin().getPluginArtifact();
    }
    
    private ContainerManagedPlugin getContianerManagedPlugin()
    {
        return (ContainerManagedPlugin) theConnectPlugin;
    }
}
