package com.atlassian.plugin.connect.plugin.module;

import java.util.Set;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.impl.AbstractDelegatingPlugin;
import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A decorator for addon bundles that delegates all autowiring requests to the actual connect plugin
 */
public class AutowireWithConnectPluginDecorator extends AbstractDelegatingPlugin implements ContainerManagedPlugin
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ContainerManagedPlugin theConnectPlugin;
    private final Set<Class<?>> productConditions;

    public AutowireWithConnectPluginDecorator(ContainerManagedPlugin theConnectPlugin, Plugin delegate, Set<Class<?>> productConditions)
    {
        super(delegate);
        this.theConnectPlugin = theConnectPlugin;
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
