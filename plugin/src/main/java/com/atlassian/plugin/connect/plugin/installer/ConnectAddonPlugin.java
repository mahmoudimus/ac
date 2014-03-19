package com.atlassian.plugin.connect.plugin.installer;

import java.io.InputStream;
import java.net.URL;

import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.impl.AbstractPlugin;

public class ConnectAddonPlugin extends AbstractPlugin
{
    private PluginState pluginState;

    public ConnectAddonPlugin()
    {
        this.pluginState = PluginState.DISABLED;
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
        return true;
    }
    
    @Override
    public boolean isDynamicallyLoaded()
    {
        return false;
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

    @Override
    public void setPluginState(PluginState state)
    {
        this.pluginState = state;
    }

    @Override
    public PluginState getPluginState()
    {
        return pluginState;
    }
}
