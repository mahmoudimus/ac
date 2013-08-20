package com.atlassian.plugin.connect.plugin.settings;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class SettingsManager
{
    private final PluginSettingsFactory pluginSettingsFactory;

    @Autowired
    public SettingsManager(PluginSettingsFactory pluginSettingsFactory)
    {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    public void setAllowDogfooding(boolean allowed)
    {
        pluginSettingsFactory.createGlobalSettings().put("atlassian-connect-settings-allow-dogfooding", String.valueOf(allowed));
    }

    public boolean isAllowDogfooding()
    {
        return Boolean.valueOf((String) pluginSettingsFactory.createGlobalSettings().get("atlassian-connect-settings-allow-dogfooding"));
    }
}
