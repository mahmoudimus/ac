package com.atlassian.plugin.connect.plugin.installer;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.base.Strings;

@Named
@ExportAsDevService
public class DefaultConnectAddonRegistry implements ConnectAddonRegistry
{
    private static final String CONNECT_DESCRIPTOR_PREFIX = "ac.desc.";
    private static final String CONNECT_BASEURL_PREFIX = "ac.baseurl.";
    private static final String CONNECT_SECRET_PREFIX = "ac.secret.";

    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public DefaultConnectAddonRegistry(PluginSettingsFactory pluginSettingsFactory)
    {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    public void removeAll(String pluginKey)
    {
        settings().remove(descriptorKey(pluginKey));
        settings().remove(baseUrlKey(pluginKey));
        settings().remove(secretKey(pluginKey));
    }
    
    @Override
    public void storeDescriptor(String pluginKey, String json)
    {
        settings().put(descriptorKey(pluginKey), json);
    }

    @Override
    public void removeDescriptor(String pluginKey)
    {
        settings().remove(descriptorKey(pluginKey));
    }

    @Override
    public String getDescriptor(String pluginKey)
    {
        return Strings.nullToEmpty((String) settings().get(descriptorKey(pluginKey)));
    }

    @Override
    public boolean hasDescriptor(String pluginKey)
    {
        return !Strings.isNullOrEmpty(getDescriptor(pluginKey));
    }

    @Override
    public void storeBaseUrl(String pluginKey, String url)
    {
        settings().put(baseUrlKey(pluginKey), url);
    }

    @Override
    public void removeBaseUrl(String pluginKey)
    {
        settings().remove(baseUrlKey(pluginKey));
    }

    @Override
    public String getBaseUrl(String pluginKey)
    {
        return Strings.nullToEmpty((String) settings().get(baseUrlKey(pluginKey)));
    }

    @Override
    public boolean hasBaseUrl(String pluginKey)
    {
        return !Strings.isNullOrEmpty(getBaseUrl(pluginKey));
    }

    @Override
    public void storeSecret(String pluginKey, String secret)
    {
        settings().put(secretKey(pluginKey), secret);
    }

    @Override
    public void removeSecret(String pluginKey)
    {
        settings().remove(secretKey(pluginKey));
    }

    @Override
    public String getSecret(String pluginKey)
    {
        return Strings.nullToEmpty((String) settings().get(secretKey(pluginKey)));
    }

    @Override
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
