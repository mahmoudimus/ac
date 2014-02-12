package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.base.Strings;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@ExportAsDevService
public class DefaultConnectAddonRegistry implements ConnectAddonRegistry
{
    private static final String CONNECT_DESCRIPTOR_PREFIX = "ac.desc.";
    private static final String CONNECT_BASEURL_PREFIX = "ac.baseurl.";
    private static final String CONNECT_SECRET_PREFIX = "ac.secret.";
    private static final String CONNECT_USER_PREFIX = "ac.user.";
    private static final String CONNECT_AUTH_PREFIX = "ac.auth.";

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
        settings().remove(key(CONNECT_USER_PREFIX, pluginKey));
        settings().remove(key(CONNECT_AUTH_PREFIX, pluginKey));
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

    @Override
    public void storeUserKey(String pluginKey, String userKey)
    {
        settings().put(key(CONNECT_USER_PREFIX, pluginKey), userKey);
    }

    @Override
    public void removeUserKey(String pluginKey)
    {
        settings().remove(key(CONNECT_USER_PREFIX, pluginKey));
    }

    @Override
    public String getUserKey(String pluginKey)
    {
        return get(key(CONNECT_USER_PREFIX, pluginKey));
    }

    @Override
    public boolean hasUserKey(String pluginKey)
    {
        return has(getUserKey(pluginKey));
    }

    @Override
    public void storeAuthType(String pluginKey, AuthenticationType type)
    {
        settings().put(key(CONNECT_AUTH_PREFIX, pluginKey), type.name());
    }

    @Override
    public void removeAuthType(String pluginKey)
    {
        settings().remove(key(CONNECT_AUTH_PREFIX, pluginKey));
    }

    @Override
    public AuthenticationType getAuthType(String pluginKey)
    {
        return AuthenticationType.valueOf(get(key(CONNECT_AUTH_PREFIX, pluginKey)));
    }

    @Override
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
