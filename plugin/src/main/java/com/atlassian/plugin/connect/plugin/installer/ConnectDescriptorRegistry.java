package com.atlassian.plugin.connect.plugin.installer;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

@Named
public class ConnectDescriptorRegistry
{
    public static final String CONNECT_DESCRIPTOR_KEY = "atlassian.connect.descriptor.registry";
    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public ConnectDescriptorRegistry(PluginSettingsFactory pluginSettingsFactory)
    {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }
    
    public void storeDescriptor(String pluginKey, String json)
    {
        Map<String,String> descriptorMap = getDescriptorMap();
        
        descriptorMap.put(pluginKey,json);
        
        saveDescriptorMap(descriptorMap);
    }

    private void saveDescriptorMap(Map<String, String> descriptorMap)
    {
        PluginSettings settings = getGlobalSettings();

        settings.remove(CONNECT_DESCRIPTOR_KEY);
        settings.put(CONNECT_DESCRIPTOR_KEY,descriptorMap);
    }

    public void removeDescriptor(String pluginKey)
    {
        Map<String,String> descriptorMap = getDescriptorMap();

        descriptorMap.remove(pluginKey);

        saveDescriptorMap(descriptorMap);
    }
    
    public boolean hasDescriptor(String pluginKey)
    {
        return getDescriptorMap().containsKey(pluginKey);
    }

    public String getDescriptor(String pluginKey)
    {
        if(hasDescriptor(pluginKey))
        {
            return getDescriptorMap().get(pluginKey);
        }
        
        return "";
    }

    private Map<String, String> getDescriptorMap()
    {
        return (Map<String, String>) getGlobalSettings().get(CONNECT_DESCRIPTOR_KEY);
    }
    
    private PluginSettings getGlobalSettings()
    {
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        if(null == settings.get(CONNECT_DESCRIPTOR_KEY))
        {
            settings.put(CONNECT_DESCRIPTOR_KEY,new HashMap<String,String>());
        }
        
        return settings;
    }

}
