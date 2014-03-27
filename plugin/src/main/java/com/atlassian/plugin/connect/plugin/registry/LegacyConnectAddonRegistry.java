package com.atlassian.plugin.connect.plugin.registry;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import com.google.common.base.Strings;

//TODO: Remove this class after the initial deploy of file-less addons
@Named
public class LegacyConnectAddonRegistry
{
    private static final String CONNECT_DESCRIPTOR_PREFIX = "ac.desc.";
    private static final String CONNECT_BASEURL_PREFIX = "ac.baseurl.";
    private static final String CONNECT_SECRET_PREFIX = "ac.secret.";
    private static final String CONNECT_USER_PREFIX = "ac.user.";
    private static final String CONNECT_AUTH_PREFIX = "ac.auth.";

    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public LegacyConnectAddonRegistry(PluginSettingsFactory pluginSettingsFactory)
    {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    public void removeAll(String pluginKey)
    {
        PluginSettings settings = settings();
        settings.remove(key(CONNECT_DESCRIPTOR_PREFIX, pluginKey));
        settings.remove(key(CONNECT_BASEURL_PREFIX, pluginKey));
        settings.remove(key(CONNECT_SECRET_PREFIX, pluginKey));
        settings.remove(key(CONNECT_USER_PREFIX, pluginKey));
        settings.remove(key(CONNECT_AUTH_PREFIX, pluginKey));
    }

    public String getDescriptor(String pluginKey)
    {
        return get(key(CONNECT_DESCRIPTOR_PREFIX, pluginKey));
    }

    public boolean hasDescriptor(String pluginKey)
    {
        return has(getDescriptor(pluginKey));
    }

    public String getBaseUrl(String pluginKey)
    {
        return get(key(CONNECT_BASEURL_PREFIX, pluginKey));
    }

    public boolean hasBaseUrl(String pluginKey)
    {
        return has(getBaseUrl(pluginKey));
    }

    public String getSecret(String pluginKey)
    {
        return get(key(CONNECT_SECRET_PREFIX, pluginKey));
    }

    public boolean hasSecret(String pluginKey)
    {
        return has(getSecret(pluginKey));
    }

    public String getUserKey(String pluginKey)
    {
        return get(key(CONNECT_USER_PREFIX, pluginKey));
    }

    public boolean hasUserKey(String pluginKey)
    {
        return has(getUserKey(pluginKey));
    }

    public AuthenticationType getAuthType(String pluginKey)
    {
        return AuthenticationType.valueOf(get(key(CONNECT_AUTH_PREFIX, pluginKey)));
    }

    public boolean hasAuthType(String pluginKey)
    {
        return has(get(key(CONNECT_AUTH_PREFIX, pluginKey)));
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
