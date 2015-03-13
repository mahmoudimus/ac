package com.atlassian.plugin.connect.plugin.registry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.installer.AddonSettings;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonBeanFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;

@Named
@ExportAsDevService
public class DefaultConnectAddonRegistry implements ConnectAddonRegistry
{
    private static final String ADDON_LIST_KEY = "ac.addon.list";
    private static final String ADDON_KEY_PREFIX = "acnct.";

    private final PluginSettingsFactory pluginSettingsFactory;
    private final ConnectAddonBeanFactory connectAddonBeanFactory;

    @Inject
    public DefaultConnectAddonRegistry(PluginSettingsFactory pluginSettingsFactory, ConnectAddonBeanFactory connectAddonBeanFactory)
    {
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.connectAddonBeanFactory = connectAddonBeanFactory;
    }

    @Override
    public void removeAll(String pluginKey)
    {
        PluginSettings settings = settings();
        settings.remove(addonStorageKey(pluginKey));

        Set<String> addonKeys = getAddonKeySet(settings);
        addonKeys.remove(pluginKey);

        settings.put(ADDON_LIST_KEY, new ArrayList<String>(addonKeys));
    }

    private Set<String> getAddonKeySet(PluginSettings settings)
    {
        List<String> keyList = (List<String>) settings.get(ADDON_LIST_KEY);

        if (null == keyList)
        {
            return new HashSet<String>();
        }

        return new LinkedHashSet<String>(keyList);
    }

    @Override
    public void storeDescriptor(String pluginKey, String json)
    {
        storeAddonSettings(pluginKey, getAddonSettings(pluginKey).setDescriptor(json));
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
        storeAddonSettings(pluginKey, getAddonSettings(pluginKey).setUserKey(userKey));
    }

    @Override
    public void removeUserKey(String pluginKey)
    {
        storeAddonSettings(pluginKey, getAddonSettings(pluginKey).setUserKey(""));
    }

    @Override
    public String getUserKey(String pluginKey)
    {
        return getAddonSettings(pluginKey).getUserKey();
    }

    @Override
    public boolean hasUserKey(String pluginKey)
    {
        return has(getUserKey(pluginKey));
    }

    @Override
    public void storeAuthType(String pluginKey, AuthenticationType type)
    {
        storeAddonSettings(pluginKey, getAddonSettings(pluginKey).setAuth(type.name()));
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
        return getAddonKeySet(settings());
    }

    @Override
    public boolean hasAddons()
    {
        List<String> keyList = (List<String>) settings().get(ADDON_LIST_KEY);
        if (null == keyList)
        {
            return false;
        }

        return !keyList.isEmpty();
    }

    @Override
    public Iterable<ConnectAddonBean> getAllAddonBeans()
    {
        PluginSettings settings = settings();
        Gson gson = new Gson();

        ImmutableList.Builder<ConnectAddonBean> addons = ImmutableList.builder();
        for (String addonKey : getAddonKeySet(settings))
        {
            AddonSettings addonSettings = this.getAddonSettings(addonKey, settings, gson);
            for (ConnectAddonBean addonBean : this.getAddonBeanFromSettings(addonSettings))
            {
                addons.add(addonBean);
            }
        }

        return addons.build();
    }

    @Override
    public void storeRestartState(String pluginKey, PluginState state)
    {
        storeAddonSettings(pluginKey, getAddonSettings(pluginKey).setRestartState(state.name()));
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
        PluginSettings settings = settings();
        Gson gson = new Gson();

        ImmutableList.Builder<String> addonsToEnable = ImmutableList.builder();

        for (String addonKey : getAddonKeySet(settings))
        {
            String json = (String) settings.get(addonStorageKey(addonKey));

            if (!Strings.isNullOrEmpty(json))
            {
                AddonSettings addonSettings = gson.fromJson(json, AddonSettings.class);
                if (PluginState.ENABLED.name().equals(addonSettings.getRestartState()))
                {
                    addonsToEnable.add(addonKey);
                }
            }
        }

        return addonsToEnable.build();
    }

    private boolean has(String value)
    {
        return !Strings.isNullOrEmpty(value);
    }

    private PluginSettings settings()
    {
        return pluginSettingsFactory.createGlobalSettings();
    }

    private String addonStorageKey(String addonKey)
    {
        return ADDON_KEY_PREFIX + addonKey;
    }

    public void storeAddonSettings(String pluginKey, AddonSettings addonSettings)
    {
        String settingsToStore = new Gson().toJson(addonSettings);

        PluginSettings settings = settings();
        settings.put(addonStorageKey(pluginKey), settingsToStore);

        Set<String> addonSet = getAddonKeySet(settings);
        addonSet.add(pluginKey);

        settings.put(ADDON_LIST_KEY, new ArrayList<String>(addonSet));

    }

    @Override
    public AddonSettings getAddonSettings(String pluginKey)
    {
        return getAddonSettings(pluginKey, settings(), new Gson());
    }

    @Override
    public Option<ConnectAddonBean> getAddonBean(String pluginKey)
    {
        AddonSettings addonSettings = getAddonSettings(pluginKey);
        return getAddonBeanFromSettings(addonSettings);
    }

    private AddonSettings getAddonSettings(String pluginKey, PluginSettings settings, Gson gson)
    {
        AddonSettings addonSettings = new AddonSettings();
        String json = (String) settings.get(addonStorageKey(pluginKey));
        if (!Strings.isNullOrEmpty(json))
        {
            addonSettings = gson.fromJson(json, AddonSettings.class);
        }
        return addonSettings;
    }

    private Option<ConnectAddonBean> getAddonBeanFromSettings(AddonSettings addonSettings)
    {
        Option<ConnectAddonBean> beanOption;
        String descriptor = addonSettings.getDescriptor();
        if (!Strings.isNullOrEmpty(descriptor))
        {
            beanOption = some(connectAddonBeanFactory.fromJsonSkipValidation(addonSettings.getDescriptor()));
        }
        else
        {
            beanOption = none(ConnectAddonBean.class);
        }
        return beanOption;
    }
    @Override
    public boolean hasAddonWithKey(final String pluginKey)
    {
        return getAddonKeySet(settings()).contains(pluginKey);
    }

}
