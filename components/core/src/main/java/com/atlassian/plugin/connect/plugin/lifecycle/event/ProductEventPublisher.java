package com.atlassian.plugin.connect.plugin.lifecycle.event;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Publishes product and framework upgraded events when the host starts
 */
@ExportAsService
@Named
public class ProductEventPublisher implements LifecycleAware {
    public static final String PLUGINS_LAST_VERSION = "plugins.lastVersion";
    private final EventPublisher eventPublisher;
    private final ApplicationProperties applicationProperties;
    private final PluginSettingsFactory pluginSettingsFactory;
    private PluginRetrievalService pluginRetrievalService;
    private volatile boolean started = false;

    @Inject
    public ProductEventPublisher(EventPublisher eventPublisher,
                                 ApplicationProperties applicationProperties,
                                 PluginSettingsFactory pluginSettingsFactory,
                                 PluginRetrievalService pluginRetrievalService) {
        this.eventPublisher = eventPublisher;
        this.applicationProperties = applicationProperties;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.pluginRetrievalService = pluginRetrievalService;
    }

    @Override
    public void onStart() {
        if (!started) {
            String lastProductVersion = getLastVersion("server");
            String currentProductVersion = getCurrentServerVersion();
            if (!currentProductVersion.equals(lastProductVersion)) {
                eventPublisher.publish(new ServerUpgradedEvent(lastProductVersion, currentProductVersion));
                saveCurrentVersion("server", currentProductVersion);
            }

            String lastVersion = getLastVersion("plugins");
            String currentVersion = pluginRetrievalService.getPlugin().getPluginInformation().getVersion();
            if (!currentVersion.equals(lastVersion)) {
                eventPublisher.publish(new PluginsUpgradedEvent(lastVersion, currentVersion));
                saveCurrentVersion("plugins", currentVersion);
            }
            started = true;
        }
    }

    @Override
    public void onStop() {
        started = false;
    }

    private void saveCurrentVersion(String type, String currentVersion) {
        pluginSettingsFactory.createGlobalSettings().put(getSettingsKey(type), currentVersion);
    }

    private String getSettingsKey(String type) {
        return PLUGINS_LAST_VERSION + "." + type;
    }

    private String getCurrentServerVersion() {
        return applicationProperties.getBuildNumber();
    }

    private String getLastVersion(String type) {
        String value = (String) pluginSettingsFactory.createGlobalSettings().get(
                getSettingsKey(type));
        return value != null ? value : "";
    }
}
