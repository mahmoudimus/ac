package com.atlassian.plugin.connect.plugin.registry;

import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.installer.AddonSettings;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonBeanFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@ExportAsDevService
public class DefaultConnectAddonRegistry implements ConnectAddonRegistry
{
    private final ConnectAddonEntityService addonEntityService;
    private final ConnectAddonBeanFactory connectAddonBeanFactory;

    @Inject
    public DefaultConnectAddonRegistry(ConnectAddonEntityService addonEntityService, ConnectAddonBeanFactory connectAddonBeanFactory)
    {
        this.addonEntityService = addonEntityService;
        this.connectAddonBeanFactory = connectAddonBeanFactory;
    }

    @Override
    public void removeAll(String pluginKey)
    {
        addonEntityService.delete(pluginKey);
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
        return addonEntityService.getAddonKeys();
    }

    @Override
    public Iterable<ConnectAddonBean> getAllAddonBeans()
    {
        Gson gson = new Gson();

        ImmutableList.Builder<ConnectAddonBean> addons = ImmutableList.builder();
        for (ConnectAddonEntity entity : addonEntityService.getAllAddons())
        {
            AddonSettings settings = gson.fromJson(entity.getSettings(), AddonSettings.class);
            if (has(settings.getDescriptor()))
            {
                addons.add(connectAddonBeanFactory.fromJsonSkipValidation(settings.getDescriptor()));
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
        Gson gson = new Gson();

        ImmutableList.Builder<String> addonsToEnable = ImmutableList.builder();
        for (ConnectAddonEntity entity : addonEntityService.getAllAddons())
        {
            AddonSettings settings = gson.fromJson(entity.getSettings(), AddonSettings.class);
            if (PluginState.ENABLED.name().equals(settings.getRestartState()))
            {
                addonsToEnable.add(entity.getAddonKey());
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
        String settingsToStore = new Gson().toJson(addonSettings);

        addonEntityService.createOrUpdate(pluginKey, settingsToStore);
    }

    private AddonSettings getAddonSettings(String pluginKey)
    {
        AddonSettings addonsettings = new AddonSettings();

        ConnectAddonEntity entity = addonEntityService.get(pluginKey);

        if (null != entity)
        {
            addonsettings = new Gson().fromJson(entity.getSettings(), AddonSettings.class);
        }

        return addonsettings;
    }

}
