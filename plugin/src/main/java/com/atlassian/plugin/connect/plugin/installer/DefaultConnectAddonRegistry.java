package com.atlassian.plugin.connect.plugin.installer;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
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
    private static final String CONNECT_USER_PREFIX = "ac.user.";
    private static final String CONNECT_AUTH_PREFIX = "ac.auth.";
    private static final String CONNECT_PLUGINSTOENABLE_KEY = "ac.pluginsToEnable";

    private final PluginSettingsFactory pluginSettingsFactory;
    private File pluginsToEnableFile;
    private final Charset utf8;

    @Inject
    public DefaultConnectAddonRegistry(PluginSettingsFactory pluginSettingsFactory)
    {
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.utf8 = Charset.forName("UTF-8");
    }

    @Override
    public void removeAll(String pluginKey)
    {
        PluginSettings settings = settings();
        settings.remove(key(CONNECT_DESCRIPTOR_PREFIX, pluginKey));
        settings.remove(key(CONNECT_BASEURL_PREFIX, pluginKey));
        settings.remove(key(CONNECT_SECRET_PREFIX, pluginKey));
        settings.remove(key(CONNECT_USER_PREFIX, pluginKey));
        settings.remove(key(CONNECT_AUTH_PREFIX, pluginKey));
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

    @Override
    public void storePluginsToEnable(List<String> pluginKeys)
    {
        settings().put(CONNECT_PLUGINSTOENABLE_KEY, pluginKeys);
    }

    @Override
    public void removePluginsToEnable()
    {
        settings().remove(CONNECT_PLUGINSTOENABLE_KEY);
    }

    @Override
    public List<String> getPluginsToEnable()
    {
        List<String> pluginsToEnable = (List<String>) settings().get(CONNECT_PLUGINSTOENABLE_KEY);

        if (null == pluginsToEnable)
        {
            pluginsToEnable = Collections.emptyList();
        }

        return pluginsToEnable;
    }

    @Override
    public boolean hasPluginsToEnable()
    {
        List<String> value = getPluginsToEnable();
        boolean hasPlugins = (null != value && !value.isEmpty());

        return hasPlugins;
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
