package com.atlassian.plugin.remotable.plugin.license;

import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.remotable.host.common.util.BundleUtil;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;
import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Retrieves a license for a given plugin
 */
@Component
public class LicenseRetriever implements DisposableBean
{
    private final PluginEventManager pluginEventManager;
    private final Cache<String, ServiceTracker> licenseManagers;
    private static final Logger log = LoggerFactory.getLogger(LicenseRetriever.class);

    @Autowired
    public LicenseRetriever(final BundleContext bundleContext, PluginEventManager pluginEventManager)
    {
        this.pluginEventManager = pluginEventManager;
        licenseManagers = CacheBuilder.newBuilder().build(new CacheLoader<String, ServiceTracker>()
        {
            @Override
            public ServiceTracker load(final String pluginKey) throws Exception
            {
                Bundle pluginBundle = BundleUtil.findBundleForPlugin(bundleContext, pluginKey);
                ServiceTracker tracker = new ServiceTracker(pluginBundle.getBundleContext(), PluginLicenseManager.class.getName(), null);
                tracker.open();
                return tracker;
            }
        });
        pluginEventManager.register(this);
    }

    @PluginEventListener
    public void onPluginDisabled(PluginDisabledEvent event)
    {
        String pluginKey = event.getPlugin().getKey();
        if (licenseManagers.asMap().containsKey(pluginKey))
        {
            ServiceTracker tracker = licenseManagers.getUnchecked(pluginKey);
            tracker.close();
            licenseManagers.invalidate(pluginKey);
        }
    }

    public Option<PluginLicense> getLicense(String pluginKey)
    {
        PluginLicenseManager licenseManager = (PluginLicenseManager) licenseManagers.getUnchecked(pluginKey).getService();
        if (licenseManager != null)
        {
            return licenseManager.getLicense();
        }
        else
        {
            log.warn("No plugin license manager found for '{}'", pluginKey);
            return Option.none();
        }
    }

    public LicenseStatus getLicenseStatus(String pluginKey)
    {
        return getLicense(pluginKey).map(new Function<PluginLicense, LicenseStatus>()
        {
            @Override
            public LicenseStatus apply(final PluginLicense input)
            {
                // todo: is maintenance expired the right thing to switch on?
                return input.isMaintenanceExpired() ? LicenseStatus.EXPIRED : LicenseStatus.ACTIVE;
            }
        }).getOrElse(LicenseStatus.NONE);
    }



    @Override
    public void destroy() throws Exception
    {
        pluginEventManager.unregister(this);
    }
}
