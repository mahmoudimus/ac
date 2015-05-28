package com.atlassian.plugin.connect.core.module;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.impl.AbstractDelegatingPlugin;
import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;

/**
 * A decorator for addon bundles that delegates all autowiring requests to the actual connect plugin
 */
public class AutowireWithConnectPluginDecorator extends AbstractDelegatingPlugin implements ContainerManagedPlugin
{
    private final ContainerManagedPlugin theConnectPlugin;

    public AutowireWithConnectPluginDecorator(ContainerManagedPlugin theConnectPlugin, Plugin delegate)
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
