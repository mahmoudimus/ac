package com.atlassian.plugin.connect.plugin.installer;

import java.util.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;

@Named
@ExportAsDevService
public class DefaultConnectAddonRegistry implements ConnectAddonRegistry
{
    // TODO: remove these after the initial deploy of file-less addons
    public static final String CONNECT_DESCRIPTOR_PREFIX = "ac.desc.";
    public static final String CONNECT_BASEURL_PREFIX = "ac.baseurl.";
    public static final String CONNECT_SECRET_PREFIX = "ac.secret.";
    public static final String CONNECT_USER_PREFIX = "ac.user.";
    public static final String CONNECT_AUTH_PREFIX = "ac.auth.";

    private static final String CONNECT_ADDONS_KEY = "ac.addons";

    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public DefaultConnectAddonRegistry(PluginSettingsFactory pluginSettingsFactory)
    {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    public void removeAll(String pluginKey)
    {
        Map<String,String> addons = getAddonMap();
        
        if(addons.containsKey(pluginKey))
        {
            addons.remove(pluginKey);
        }
        
        settings().put(CONNECT_ADDONS_KEY,addons);
    }

    @Override
    public void storeDescriptor(String pluginKey, String json)
    {
        storeAddonSettings(pluginKey,getAddonSettings(pluginKey).setDescriptor(json));
    }

    @Override
    public void removeDescriptor(String pluginKey)
    {
        storeAddonSettings(pluginKey, getAddonSettings(pluginKey).setDescriptor(""));
    }

    @Override
    public String getDescriptor(String pluginKey)
    {
        return getAddonSettings(pluginKey).getDescriptor();
    }

    @Override
    public boolean hasDescriptor(String pluginKey)
    {
        return has(getDescriptor(pluginKey));
    }

    @Override
    public void storeBaseUrl(String pluginKey, String url)
    {
        storeAddonSettings(pluginKey, getAddonSettings(pluginKey).setBaseUrl(url));
    }

    @Override
    public void removeBaseUrl(String pluginKey)
    {
        storeAddonSettings(pluginKey, getAddonSettings(pluginKey).setBaseUrl(""));
    }

    @Override
    public String getBaseUrl(String pluginKey)
    {
        return getAddonSettings(pluginKey).getBaseUrl();
    }

    @Override
    public boolean hasBaseUrl(String pluginKey)
    {
        return has(getBaseUrl(pluginKey));
    }

    @Override
    public void storeSecret(String pluginKey, String secret)
    {
        storeAddonSettings(pluginKey, getAddonSettings(pluginKey).setSecret(secret));
    }

    @Override
    public void removeSecret(String pluginKey)
    {
        storeAddonSettings(pluginKey, getAddonSettings(pluginKey).setSecret(""));
    }

    @Override
    public String getSecret(String pluginKey)
    {
        return getAddonSettings(pluginKey).getSecret();
    }

    @Override
    public boolean hasSecret(String pluginKey)
    {
        return has(getSecret(pluginKey));
    }

    @Override
    public void storeUserKey(String pluginKey, String userKey)
    {
        storeAddonSettings(pluginKey, getAddonSettings(pluginKey).setUser(userKey));
    }

    @Override
    public void removeUserKey(String pluginKey)
    {
        storeAddonSettings(pluginKey, getAddonSettings(pluginKey).setUser(""));
    }

    @Override
    public String getUserKey(String pluginKey)
    {
        return getAddonSettings(pluginKey).getUser();
    }

    @Override
    public boolean hasUserKey(String pluginKey)
    {
        return has(getUserKey(pluginKey));
    }

    @Override
    public void storeAuthType(String pluginKey, AuthenticationType type)
    {
        storeAddonSettings(pluginKey,getAddonSettings(pluginKey).setAuth(type.name()));
    }

    @Override
    public void removeAuthType(String pluginKey)
    {
        storeAddonSettings(pluginKey, getAddonSettings(pluginKey).setAuth(""));
    }

    @Override
    public AuthenticationType getAuthType(String pluginKey)
    {
        return AuthenticationType.valueOf(getAddonSettings(pluginKey).getAuth());
    }

    @Override
    public boolean hasAuthType(String pluginKey)
    {
        return has(getAddonSettings(pluginKey).getAuth());
    }

    @Override
    public Iterable<String> getAllAddonKeys()
    {
        return getAddonMap().keySet();
    }

    @Override
    public void storeRestartState(String pluginKey, PluginState state)
    {
        storeAddonSettings(pluginKey,getAddonSettings(pluginKey).setRestartState(state.name()));
    }

    @Override
    public void removeRestartState(String pluginKey)
    {
        storeAddonSettings(pluginKey, getAddonSettings(pluginKey).setRestartState(""));
    }

    @Override
    public PluginState getRestartState(String pluginKey)
    {
        return PluginState.valueOf(getAddonSettings(pluginKey).getRestartState());
    }

    @Override
    public Iterable<String> getAddonKeysToEnableOnRestart()
    {
        Gson gson = new Gson();

        ImmutableList.Builder<String> addonsToEnable = ImmutableList.builder();
        for(Map.Entry<String,String> entry : getAddonMap().entrySet())
        {
            AddonSettings settings = gson.fromJson(entry.getValue(),AddonSettings.class);
            if(PluginState.ENABLED.name().equals(settings.getRestartState()))
            {
                addonsToEnable.add(entry.getKey());
            }
        }
        
        return addonsToEnable.build();
    }

    private boolean has(String value)
    {
        return !Strings.isNullOrEmpty(value);
    }

    public void storeAddonSettings(String pluginKey, AddonSettings addonSettings)
    {
        Map<String,String> addons = getAddonMap();
        
        String settingsToStore = new Gson().toJson(addonSettings);
        
        addons.put(pluginKey,settingsToStore);
        
        settings().put(CONNECT_ADDONS_KEY,addons);
    }
    
    private AddonSettings getAddonSettings(String pluginKey)
    {
        AddonSettings addonsettings = new AddonSettings();
        
        Map<String,String> addons = (Map<String,String>) settings().get(CONNECT_ADDONS_KEY);
        
        if(null != addons && addons.containsKey(pluginKey))
        {
            addonsettings = new Gson().fromJson(addons.get(pluginKey),AddonSettings.class);
        }
        
        return addonsettings;
    }

    private Map<String,String> getAddonMap()
    {
        Map<String,String> addons = (Map<String,String>) settings().get(CONNECT_ADDONS_KEY);

        if(null == addons)
        {
            addons = new HashMap<String, String>();
        }

        return addons;
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
