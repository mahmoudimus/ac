package com.atlassian.plugin.connect.test.fixture;

import com.atlassian.plugin.DummyPlugin;

import java.io.InputStream;
import java.net.URL;

/**
 * @since 1.0
 */
public class PluginForTests extends DummyPlugin {

    private final String pluginKey;
    private final String name;

    public PluginForTests(String pluginKey, String pluginName) {
        this.pluginKey = pluginKey;
        this.name = pluginName;
    }

    @Override
    public String getKey() {
        return pluginKey;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isUninstallable() {
        return true;
    }

    @Override
    public boolean isDeleteable() {
        return true;
    }

    @Override
    public boolean isDynamicallyLoaded() {
        return false;
    }

    @Override
    public <T> Class<T> loadClass(String clazz, Class<?> callingClass) throws ClassNotFoundException {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public URL getResource(String path) {
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return null;
    }
}
