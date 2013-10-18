package com.atlassian.plugin.connect.plugin.module;

import java.util.Set;

import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.impl.AbstractDelegatingPlugin;
import com.atlassian.plugin.connect.spi.module.UserIsLoggedInCondition;
import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin that can load conditions from the remotable plugins plugin
 */
public class ConditionLoadingPlugin extends AbstractDelegatingPlugin implements AutowireCapablePlugin, ContainerManagedPlugin
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AutowireCapablePlugin theConnectPlugin;
    private final Set<Class<?>> productConditions;

    public ConditionLoadingPlugin(AutowireCapablePlugin theConnectPlugin, Plugin delegate, Set<Class<?>> productConditions)
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

    @Override
    public <T> T autowire(Class<T> clazz) throws UnsupportedOperationException
    {
        if (isRemotablePluginCondition(clazz))
        {
            return theConnectPlugin.autowire(clazz);
        }
        return getAutowireCapableDelegate().autowire(clazz);
    }

    @Override
    public <T> T autowire(Class<T> clazz, AutowireStrategy autowireStrategy) throws UnsupportedOperationException
    {
        if (isRemotablePluginCondition(clazz))
        {
            return theConnectPlugin.autowire(clazz, autowireStrategy);
        }
        return getAutowireCapableDelegate().autowire(clazz, autowireStrategy);
    }

    @Override
    public void autowire(Object instance)
    {
        getAutowireCapableDelegate().autowire(instance);
    }

    @Override
    public void autowire(Object instance, AutowireStrategy autowireStrategy)
    {
        getAutowireCapableDelegate().autowire(instance, autowireStrategy);
    }

    private <T> boolean isRemotablePluginCondition(Class<T> clazz)
    {
        return isInRemotablePluginConditionsPackage(clazz) || productConditions.contains(clazz);
    }

    private <T> boolean isInRemotablePluginConditionsPackage(Class clazz)
    {
        final Package clazzPackage = clazz.getPackage();
        final Package remotablePluginConditionPackage = UserIsLoggedInCondition.class.getPackage();
        final boolean isRemotablePluginConditionPackage = clazzPackage.equals(remotablePluginConditionPackage);
        if (!isRemotablePluginConditionPackage && clazzPackage.getName().equals(remotablePluginConditionPackage.getName()))
        {
            logger.warn("Class '{}' package is not equal to '{}'. Yet they have the same name. This is probably not what "
                    + "you expected, as it means those were not loaded by the same classloader. For you information the class was loaded "
                    + "from classloader: {} while the package is from classloader: {}",
                    new Object[]{clazz.getName(), remotablePluginConditionPackage.getName(), clazz.getClassLoader(), UserIsLoggedInCondition.class.getClassLoader()});
        }

        return isRemotablePluginConditionPackage;
    }

    private AutowireCapablePlugin getAutowireCapableDelegate()
    {
        return ((AutowireCapablePlugin) getDelegate());
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> cast(Class<?> aClass)
    {
        return (Class<T>) aClass;
    }

    @Override
    public ContainerAccessor getContainerAccessor()
    {
        return ((ContainerManagedPlugin)getDelegate()).getContainerAccessor();
    }

    @Override
    public PluginArtifact getPluginArtifact()
    {
        return ((ContainerManagedPlugin)getDelegate()).getPluginArtifact();
    }
}
