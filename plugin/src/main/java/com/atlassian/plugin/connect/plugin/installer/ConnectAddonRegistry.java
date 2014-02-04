package com.atlassian.plugin.connect.plugin.installer;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.base.Strings;

@Named
public class ConnectAddonRegistry
{
    private static final String CONNECT_DESCRIPTOR_PREFIX = "ac.desc.";
    private static final String CONNECT_BASEURL_PREFIX = "ac.baseurl.";
    private static final String CONNECT_SECRET_PREFIX = "ac.secret.";

    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public ConnectAddonRegistry(PluginSettingsFactory pluginSettingsFactory)
    {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    public void removeAll(String pluginKey)
    {
        settings().remove(descriptorKey(pluginKey));
        settings().remove(baseUrlKey(pluginKey));
        settings().remove(secretKey(pluginKey));
    }
    
    public void storeDescriptor(String pluginKey, String json)
    {
        settings().put(descriptorKey(pluginKey), json);
    }

    public void removeDescriptor(String pluginKey)
    {
        settings().remove(descriptorKey(pluginKey));
    }

    public String getDescriptor(String pluginKey)
    {
        return Strings.nullToEmpty((String) settings().get(descriptorKey(pluginKey)));
    }

    public boolean hasDescriptor(String pluginKey)
    {
        return !Strings.isNullOrEmpty(getDescriptor(pluginKey));
    }

    public void storeBaseUrl(String pluginKey, String url)
    {
        settings().put(baseUrlKey(pluginKey), url);
    }

    public void removeBaseUrl(String pluginKey)
    {
        settings().remove(baseUrlKey(pluginKey));
    }

    public String getBaseUrl(String pluginKey)
    {
        return Strings.nullToEmpty((String) settings().get(baseUrlKey(pluginKey)));
    }

    public boolean hasBaseUrl(String pluginKey)
    {
        return !Strings.isNullOrEmpty(getBaseUrl(pluginKey));
    }

    public void storeSecret(String pluginKey, String secret)
    {
        settings().put(secretKey(pluginKey), secret);
    }

    public void removeSecret(String pluginKey)
    {
        settings().remove(secretKey(pluginKey));
    }

    public String getSecret(String pluginKey)
    {
        return Strings.nullToEmpty((String) settings().get(secretKey(pluginKey)));
    }

    public boolean hasSecret(String pluginKey)
    {
        return !Strings.isNullOrEmpty(getSecret(pluginKey));
    }

    private PluginSettings settings()
    {
        return pluginSettingsFactory.createGlobalSettings();
    }

    private static String descriptorKey(String key)
    {
        return CONNECT_DESCRIPTOR_PREFIX + key;
    }

    private static String baseUrlKey(String key)
    {
        return CONNECT_BASEURL_PREFIX + key;
    }

    private static String secretKey(String key)
    {
        return CONNECT_SECRET_PREFIX + key;
    }

}
