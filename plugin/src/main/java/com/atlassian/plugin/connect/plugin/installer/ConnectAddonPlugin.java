package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.DummyPlugin;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginState;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

public class ConnectAddonPlugin extends DummyPlugin
{
    private PluginState pluginState;
    private Collection<ModuleDescriptor<?>> moduleDescriptors;

    public ConnectAddonPlugin()
    {
        this.pluginState = PluginState.DISABLED;
        this.moduleDescriptors = Collections.EMPTY_LIST;
    }

    public ConnectAddonPlugin(Collection<ModuleDescriptor<?>> moduleDescriptors)
    {
        this.pluginState = PluginState.DISABLED;
        this.moduleDescriptors = moduleDescriptors;
    }

    @Override
    public boolean isUninstallable()
    {
        return true;
    }

    @Override
    public boolean isDeleteable()
    {
        return false;
    }

    @Override
    public boolean isEnabledByDefault()
    {
        return false;
    }

    @Override
    public boolean isDynamicallyLoaded()
    {
        return true;
    }

    @Override
    public <T> Class<T> loadClass(String clazz, Class<?> callingClass) throws ClassNotFoundException
    {
        return null;
    }

    @Override
    public ClassLoader getClassLoader()
    {
        return null;
    }

    @Override
    public URL getResource(String path)
    {
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String name)
    {
        return null;
    }

    public void setPluginState(PluginState state)
    {
        this.pluginState = state;
    }

    @Override
    public PluginState getPluginState()
    {
        return pluginState;
    }

    @Override
    public Collection<ModuleDescriptor<?>> getModuleDescriptors()
    {
        return moduleDescriptors;
    }

}
