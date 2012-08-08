package com.atlassian.labs.remoteapps.modules.applinks;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.impl.AbstractDelegatingPlugin;
import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;

import java.util.Collection;

/**
* Plugin wrapper that takes over class loading
*/
class DelegatePlugin extends AbstractDelegatingPlugin implements ContainerManagedPlugin
{

    public DelegatePlugin(Plugin delegate)
    {
        super(delegate);
    }

    @Override
    public ContainerAccessor getContainerAccessor()
    {
        if (getDelegate() instanceof ContainerManagedPlugin)
        {
            return ((ContainerManagedPlugin)getDelegate()).getContainerAccessor();
        }
        else
        {
            return new ContainerAccessor()
            {
                @Override
                public <T> T createBean(Class<T> clazz)
                {
                    try
                    {
                        return clazz.newInstance();
                    }
                    catch (InstantiationException e)
                    {
                        throw new PluginParseException(e);
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new PluginParseException(e);
                    }
                }

                @Override
                public <T> Collection<T> getBeansOfType(Class<T> interfaceClass)
                {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
