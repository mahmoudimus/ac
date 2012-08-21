package com.atlassian.labs.remoteapps.spi.event.product;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

/**
 * Publishes product and framework upgraded events when the host starts
 */
public class ProductEventPublisher implements LifecycleAware
{
    public static final String REMOTEAPPS_LAST_VERSION = "remoteapps.lastVersion";
    private final EventPublisher eventPublisher;
    private final ApplicationProperties applicationProperties;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final BundleContext bundleContext;
    private volatile boolean started = false;

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

            String lastVersion = getLastVersion("remoteapps");
            String currentVersion = getCurrentRemoteAppsVersion();
            if (!currentVersion.equals(lastVersion))
            {
                eventPublisher.publish(new RemoteAppsUpgradedEvent(lastVersion, currentVersion));
                saveCurrentVersion("remoteapps", currentVersion);
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
        return REMOTEAPPS_LAST_VERSION + "." + type;
    }

    private String getCurrentServerVersion()
    {
        return applicationProperties.getBuildNumber();
    }

    private String getCurrentRemoteAppsVersion()
    {
        return (String) bundleContext.getBundle().getHeaders().get(Constants.BUNDLE_VERSION_ATTRIBUTE);
    }

    private String getLastVersion(String type)
    {
        String value = (String) pluginSettingsFactory.createGlobalSettings().get(
                getSettingsKey(type));
        return value != null ? value : "";
    }
}
