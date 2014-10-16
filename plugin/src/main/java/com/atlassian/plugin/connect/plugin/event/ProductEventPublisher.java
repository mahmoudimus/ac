package com.atlassian.plugin.connect.plugin.event;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.connect.plugin.util.BundleUtil;
import com.atlassian.plugin.connect.spi.event.product.PluginsUpgradedEvent;
import com.atlassian.plugin.connect.spi.event.product.ServerUpgradedEvent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

/**
 * Publishes product and framework upgraded events when the host starts
 */
@ExportAsService
@Named
public class ProductEventPublisher implements LifecycleAware
{
    public static final String PLUGINS_LAST_VERSION = "plugins.lastVersion";
    private final EventPublisher eventPublisher;
    private final ApplicationProperties applicationProperties;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final BundleContext bundleContext;
    private volatile boolean started = false;

    @Inject
    public ProductEventPublisher(EventPublisher eventPublisher,
            ApplicationProperties applicationProperties,
            PluginSettingsFactory pluginSettingsFactory, BundleContext bundleContext)
    {
        this.eventPublisher = eventPublisher;
        this.applicationProperties = applicationProperties;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.bundleContext = bundleContext;
    }

    @Override
    public void onStart()
    {
        if (!started)
        {
            String lastProductVersion = getLastVersion("server");
            String currentProductVersion = getCurrentServerVersion();
            if (!currentProductVersion.equals(lastProductVersion))
            {
                eventPublisher.publish(new ServerUpgradedEvent(lastProductVersion, currentProductVersion));
                saveCurrentVersion("server", currentProductVersion);
            }

            String lastVersion = getLastVersion("plugins");
            String currentVersion = getCurrentPluginsVersion();
            if (!currentVersion.equals(lastVersion))
            {
                eventPublisher.publish(new PluginsUpgradedEvent(lastVersion, currentVersion));
                saveCurrentVersion("plugins", currentVersion);
            }
            started = true;
        }
    }

    private void saveCurrentVersion(String type, String currentVersion)
    {
        pluginSettingsFactory.createGlobalSettings().put(getSettingsKey(type), currentVersion);
    }

    private String getSettingsKey(String type)
    {
        return PLUGINS_LAST_VERSION + "." + type;
    }

    private String getCurrentServerVersion()
    {
        return applicationProperties.getBuildNumber();
    }

    private String getCurrentPluginsVersion()
    {
        return BundleUtil.getBundleVersion(bundleContext);
    }

    private String getLastVersion(String type)
    {
        String value = (String) pluginSettingsFactory.createGlobalSettings().get(
                getSettingsKey(type));
        return value != null ? value : "";
    }
}
