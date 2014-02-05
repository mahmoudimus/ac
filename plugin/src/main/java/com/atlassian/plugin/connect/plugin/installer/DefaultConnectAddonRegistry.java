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
        settings().remove(key(CONNECT_DESCRIPTOR_PREFIX, pluginKey));
        settings().remove(key(CONNECT_BASEURL_PREFIX, pluginKey));
        settings().remove(key(CONNECT_SECRET_PREFIX, pluginKey));
    }
    
    @Override
    public void storeDescriptor(String pluginKey, String json)
    {
        settings().put(key(CONNECT_DESCRIPTOR_PREFIX, pluginKey), json);
    }

    @Override
    public void removeDescriptor(String pluginKey)
    {
        settings().remove(key(CONNECT_DESCRIPTOR_PREFIX, pluginKey));
    }

    @Override
    public String getDescriptor(String pluginKey)
    {
        return get(key(CONNECT_DESCRIPTOR_PREFIX, pluginKey));
    }

    @Override
    public boolean hasDescriptor(String pluginKey)
    {
        return has(getDescriptor(pluginKey));
    }

    @Override
    public void storeBaseUrl(String pluginKey, String url)
    {
        settings().put(key(CONNECT_BASEURL_PREFIX, pluginKey), url);
    }

    @Override
    public void removeBaseUrl(String pluginKey)
    {
        settings().remove(key(CONNECT_BASEURL_PREFIX, pluginKey));
    }

    @Override
    public String getBaseUrl(String pluginKey)
    {
        return get(key(CONNECT_BASEURL_PREFIX, pluginKey));
    }

    @Override
    public boolean hasBaseUrl(String pluginKey)
    {
        return has(getBaseUrl(pluginKey));
    }

    @Override
    public void storeSecret(String pluginKey, String secret)
    {
        settings().put(key(CONNECT_SECRET_PREFIX, pluginKey), secret);
    }

    @Override
    public void removeSecret(String pluginKey)
    {
        settings().remove(key(CONNECT_SECRET_PREFIX, pluginKey));
    }

    @Override
    public String getSecret(String pluginKey)
    {
        return get(key(CONNECT_SECRET_PREFIX, pluginKey));
    }

    @Override
    public boolean hasSecret(String pluginKey)
    {
        return has(getSecret(pluginKey));
    }

    private boolean has(String value)
    {
        return !Strings.isNullOrEmpty(value);
    }
    
    private String get(String key)
    {
        return Strings.nullToEmpty((String) settings().get(key));
    }
    
    private PluginSettings settings()
    {
        return pluginSettingsFactory.createGlobalSettings();
    }

    private static String key(String prefix, String key)
    {
        return prefix + key;
    }

}
