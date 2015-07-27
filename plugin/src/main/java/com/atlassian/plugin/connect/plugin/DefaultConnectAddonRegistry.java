package com.atlassian.plugin.connect.plugin;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.api.installer.AddonSettings;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonBeanFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;

@Named
@ExportAsDevService
public class DefaultConnectAddonRegistry implements ConnectAddonRegistry
{
    protected static final String ADDON_LIST_KEY = "ac.addon.list";
    protected static final String ADDON_KEY_PREFIX = "acnct.";

    private final PluginSettings settings;

    private final ConnectAddonBeanFactory connectAddonBeanFactory;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock write = readWriteLock.writeLock();
    private final Lock read = readWriteLock.readLock();

    @Inject
    public DefaultConnectAddonRegistry(PluginSettingsFactory pluginSettingsFactory, ConnectAddonBeanFactory connectAddonBeanFactory)
    {
        this.settings = pluginSettingsFactory.createGlobalSettings();
        this.connectAddonBeanFactory = connectAddonBeanFactory;
    }

    @Override
    public void removeAll(String pluginKey)
    {
        try
        {
            write.lock();
            settings.remove(addonStorageKey(pluginKey));

            Set<String> addonKeys = getAddonKeySet();
            addonKeys.remove(pluginKey);

            settings.put(ADDON_LIST_KEY, new ArrayList<>(addonKeys));
        }
        finally
        {
            write.unlock();
        }
    }

    private Set<String> getAddonKeySet()
    {
        List<String> keyList;
        try
        {
            read.lock();
            keyList = (List<String>) settings.get(ADDON_LIST_KEY);
        }
        finally
        {
            read.unlock();
        }

        if (null == keyList)
        {
            return new HashSet<>();
        }

        return new LinkedHashSet<>(keyList);
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
    public String getSecret(String pluginKey)
    {
        return getAddonSettings(pluginKey).getSecret();
    }

    @Override
    public String getUserKey(String pluginKey)
    {
        return getAddonSettings(pluginKey).getUserKey();
    }

    @Override
    public Iterable<String> getAllAddonKeys()
    {
        return getAddonKeySet();
    }

    @Override
    public boolean hasAddons()
    {
        return !getAddonKeySet().isEmpty();
    }

    @Override
    public Iterable<ConnectAddonBean> getAllAddonBeans()
    {
        Gson gson = new Gson();

        ImmutableList.Builder<ConnectAddonBean> addons = ImmutableList.builder();

        try
        {
            read.lock();
            for (String addonKey : getAddonKeySet())
            {
                AddonSettings addonSettings = this.getAddonSettings(addonKey, gson);
                for (ConnectAddonBean addonBean : this.getAddonBeanFromSettings(addonSettings))
                {
                    addons.add(addonBean);
                }
            }
        }
        finally
        {
            read.unlock();
        }

        return addons.build();
    }

    @Override
    public void storeRestartState(String pluginKey, PluginState state)
    {
        try
        {
            write.lock();

            final AddonSettings addonSettings = getAddonSettings(pluginKey);
            if (!state.toString().equalsIgnoreCase(addonSettings.getRestartState()))
            {
                addonSettings.setRestartState(state);
                storeAddonSettings(pluginKey, addonSettings);
            }
        }
        finally
        {
            write.unlock();
        }
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

        try
        {
            read.lock();
            for (String addonKey : getAddonKeySet())
            {
                final String json = getRawAddonSettings(addonKey);

                if (!Strings.isNullOrEmpty(json))
                {
                    AddonSettings addonSettings = gson.fromJson(json, AddonSettings.class);
                    if (PluginState.ENABLED.name().equals(addonSettings.getRestartState()))
                    {
                        addonsToEnable.add(addonKey);
                    }
                }
            }
        }
        finally
        {
            read.unlock();
        }

        return addonsToEnable.build();
    }

    private boolean has(String value)
    {
        return !Strings.isNullOrEmpty(value);
    }

    private String addonStorageKey(String addonKey)
    {
        return ADDON_KEY_PREFIX + addonKey;
    }

    @Override
    public void storeAddonSettings(String pluginKey, AddonSettings addonSettings)
    {
        String settingsToStore = new Gson().toJson(addonSettings);

        try
        {
            write.lock();
            settings.put(addonStorageKey(pluginKey), settingsToStore);

            Set<String> addonSet = getAddonKeySet();
            if (addonSet.add(pluginKey))
            {
                settings.put(ADDON_LIST_KEY, new ArrayList<>(addonSet));
            }
        }
        finally
        {
            write.unlock();
        }
    }

    @Override
    public AddonSettings getAddonSettings(String pluginKey)
    {
        return getAddonSettings(pluginKey, new Gson());
    }

    @Override
    public Option<ConnectAddonBean> getAddonBean(String pluginKey)
    {
        AddonSettings addonSettings = getAddonSettings(pluginKey);
        return getAddonBeanFromSettings(addonSettings);
    }

    private AddonSettings getAddonSettings(String pluginKey, Gson gson)
    {
        AddonSettings addonSettings = new AddonSettings();
        String json = getRawAddonSettings(pluginKey);

        if (!Strings.isNullOrEmpty(json))
        {
            addonSettings = gson.fromJson(json, AddonSettings.class);
        }
        return addonSettings;
    }

    private String getRawAddonSettings(String pluginKey)
    {
        String json;
        try
        {
            read.lock();
            json = (String) settings.get(addonStorageKey(pluginKey));
        }
        finally
        {
            read.unlock();
        }
        return json;
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
        return getAddonKeySet().contains(pluginKey);
    }

}
