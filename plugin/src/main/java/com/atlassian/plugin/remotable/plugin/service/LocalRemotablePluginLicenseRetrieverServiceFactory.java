package com.atlassian.plugin.remotable.plugin.service;

import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import com.atlassian.plugin.remotable.plugin.license.LicenseRetriever;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class LocalRemotablePluginLicenseRetrieverServiceFactory implements ServiceFactory
{
    private final LicenseRetriever licenseRetriever;

    public LocalRemotablePluginLicenseRetrieverServiceFactory(final LicenseRetriever licenseRetriever)
    {
        this.licenseRetriever = licenseRetriever;
    }

    @Override
    public Object getService(final Bundle bundle, final ServiceRegistration registration)
    {
        String pluginKey = OsgiHeaderUtil.getPluginKey(bundle);
        return new LocalRemotablePluginLicenseRetriever(licenseRetriever, pluginKey);
    }

    @Override
    public void ungetService(final Bundle bundle, final ServiceRegistration registration, final Object service)
    {
    }
}
