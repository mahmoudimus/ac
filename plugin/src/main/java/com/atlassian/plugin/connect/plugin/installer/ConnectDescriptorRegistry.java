package com.atlassian.plugin.connect.plugin.installer;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.base.Strings;

@Named
public class ConnectDescriptorRegistry
{
    private static final String CONNECT_DESCRIPTOR_PREFIX = "ac.desc.";

    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public ConnectDescriptorRegistry(PluginSettingsFactory pluginSettingsFactory)
    {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }
    
    public void storeDescriptor(String pluginKey, String json)
    {
        settings().put(namespace(pluginKey), json);
    }

    public void removeDescriptor(String pluginKey)
    {
        settings().remove(namespace(pluginKey));
    }

    public String getDescriptor(String pluginKey)
    {
        return Strings.nullToEmpty((String) settings().get(namespace(pluginKey)));
    }

    public boolean hasDescriptor(String pluginKey)
    {
        return !Strings.isNullOrEmpty(getDescriptor(pluginKey));
    }

    private PluginSettings settings()
    {
        return pluginSettingsFactory.createGlobalSettings();
    }

    private static String namespace(String key)
    {
        return CONNECT_DESCRIPTOR_PREFIX + key;
    }

}
