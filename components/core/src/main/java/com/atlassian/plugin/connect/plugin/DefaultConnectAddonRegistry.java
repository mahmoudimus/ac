package com.atlassian.plugin.connect.plugin;

import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Named
@ExportAsDevService
public class DefaultConnectAddonRegistry implements ConnectAddonRegistry {
    protected static final String ADDON_LIST_KEY = "ac.addon.list";
    protected static final String ADDON_KEY_PREFIX = "acnct.";

    private static final Logger log = LoggerFactory.getLogger(DefaultConnectAddonRegistry.class);

    private final PluginSettings settings;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock write = readWriteLock.writeLock();
    private final Lock read = readWriteLock.readLock();
    private final TransactionTemplate transactionTemplate;

    @Inject
    public DefaultConnectAddonRegistry(PluginSettingsFactory pluginSettingsFactory, TransactionTemplate transactionTemplate) {
        this.settings = pluginSettingsFactory.createGlobalSettings();
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void removeAll(String pluginKey) {
        write.lock();
        try {
            transactionTemplate.execute(() -> {
                settings.remove(addonStorageKey(pluginKey));

                Set<String> addonKeys = getAddonKeySet();
                addonKeys.remove(pluginKey);

                settings.put(ADDON_LIST_KEY, new ArrayList<>(addonKeys));
                return null;
            });
        } finally {
            write.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> getAddonKeySet() {
        List<String> keyList;

        read.lock();
        try {
            keyList = (List<String>) settings.get(ADDON_LIST_KEY);
        } finally {
            read.unlock();
        }

        if (null == keyList) {
            return new HashSet<>();
        }

        return new LinkedHashSet<>(keyList);
    }

    @Override
    public String getDescriptor(String pluginKey) {
        return getAddonSettings(pluginKey).getDescriptor();
    }

    @Override
    public boolean hasDescriptor(String pluginKey) {
        return has(getDescriptor(pluginKey));
    }

    @Override
    public String getBaseUrl(String pluginKey) {
        return getAddonSettings(pluginKey).getBaseUrl();
    }

    @Override
    public boolean hasBaseUrl(String pluginKey) {
        return has(getBaseUrl(pluginKey));
    }

    @Override
    public String getSecret(String pluginKey) {
        return getAddonSettings(pluginKey).getSecret();
    }

    @Override
    public String getUserKey(String pluginKey) {
        return getAddonSettings(pluginKey).getUserKey();
    }

    @Override
    public Collection<String> getAllAddonKeys() {
        return getAddonKeySet();
    }

    @Override
    public boolean hasAddons() {
        return !getAddonKeySet().isEmpty();
    }

    @Override
    public Collection<AddonSettings> getAllAddonSettings() {
        Gson gson = new Gson();

        ImmutableList.Builder<AddonSettings> allAddonSettings = ImmutableList.builder();

        read.lock();
        try {
            for (String addonKey : getAddonKeySet()) {
                final String json = getRawAddonSettings(addonKey);
                if (json != null) // paranoid; check settings actually exist
                {
                    final AddonSettings addonSettings = deserializeAddonSettings(gson, json);
                    allAddonSettings.add(addonSettings);
                } else {
                    log.warn("No settings found for listed connect add-on '{}'", addonKey);
                }
            }
        } finally {
            read.unlock();
        }

        return allAddonSettings.build();
    }

    @Override
    public void storeRestartState(String pluginKey, PluginState state) {
        write.lock();
        try {
            transactionTemplate.execute(() -> {
                final AddonSettings addonSettings = getAddonSettings(pluginKey);
                if (addonSettings.getBaseUrl().isEmpty()) {
                    log.warn("Cannot update restart state for add-on '{}'. Add-on settings not found", pluginKey);
                } else if (!state.toString().equalsIgnoreCase(addonSettings.getRestartState())) {
                    addonSettings.setRestartState(state);
                    storeAddonSettings(pluginKey, addonSettings);
                }
                return null;
            });
        } finally {
            write.unlock();
        }
    }

    @Override
    public PluginState getRestartState(String pluginKey) {
        return PluginState.valueOf(getAddonSettings(pluginKey).getRestartState());
    }

    @Override
    public Iterable<String> getAddonKeysToEnableOnRestart() {
        Gson gson = new Gson();

        ImmutableList.Builder<String> addonsToEnable = ImmutableList.builder();

        read.lock();
        try {
            for (String addonKey : getAddonKeySet()) {
                final String json = getRawAddonSettings(addonKey);

                if (!Strings.isNullOrEmpty(json)) {
                    AddonSettings addonSettings = deserializeAddonSettings(gson, json);
                    if (PluginState.ENABLED.name().equals(addonSettings.getRestartState())) {
                        addonsToEnable.add(addonKey);
                    }
                }
            }
        } finally {
            read.unlock();
        }

        return addonsToEnable.build();
    }

    private boolean has(String value) {
        return !Strings.isNullOrEmpty(value);
    }

    private String addonStorageKey(String addonKey) {
        return ADDON_KEY_PREFIX + addonKey;
    }

    @Override
    public void storeAddonSettings(String pluginKey, AddonSettings addonSettings) {
        String settingsToStore = new Gson().toJson(addonSettings);

        write.lock();
        try {
            transactionTemplate.execute(() -> {
                settings.put(addonStorageKey(pluginKey), settingsToStore);

                Set<String> addonSet = getAddonKeySet();
                if (addonSet.add(pluginKey)) {
                    settings.put(ADDON_LIST_KEY, new ArrayList<>(addonSet));
                }
                return null;
            });
        } finally {
            write.unlock();
        }
    }

    @Override
    public AddonSettings getAddonSettings(String pluginKey) {
        return getAddonSettings(pluginKey, new Gson());
    }

    private AddonSettings getAddonSettings(String pluginKey, Gson gson) {
        String json;
        read.lock();
        try {
            json = getRawAddonSettings(pluginKey);
        } finally {
            read.unlock();
        }
        if (!Strings.isNullOrEmpty(json)) {
            return deserializeAddonSettings(gson, json);
        }
        // not found - return an empty AddonSetting object
        return new AddonSettings();
    }

    private AddonSettings deserializeAddonSettings(Gson gson, String json) {
        return gson.fromJson(json, AddonSettings.class);
    }

    private String getRawAddonSettings(String pluginKey) {
        return (String) settings.get(addonStorageKey(pluginKey));
    }

    @Override
    public boolean hasAddonWithKey(final String pluginKey) {
        return getAddonKeySet().contains(pluginKey);
    }

}
