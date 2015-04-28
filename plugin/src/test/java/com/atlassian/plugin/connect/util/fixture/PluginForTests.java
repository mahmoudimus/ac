package com.atlassian.plugin.connect.util.fixture;

import java.io.InputStream;
import java.net.URL;

import com.atlassian.plugin.impl.AbstractPlugin;

/**
 * @since 1.0
 */
public class PluginForTests extends AbstractPlugin
{
    public PluginForTests(String pluginKey, String pluginName)
    {
        setKey(pluginKey);
        setName(pluginName);
        setPluginsVersion(2);
    }

    @Override
    public boolean isUninstallable()
    {
        return true;
    }

    @Override
    public boolean isDeleteable()
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
}
