package com.atlassian.plugin.connect.jira.api;

import com.atlassian.plugin.connect.api.ConnectAddonLicenseAccessor;
import com.atlassian.plugin.connect.plugin.api.LicenseStatus;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.google.common.annotations.VisibleForTesting;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
@ExportAsService(ConnectAddonLicenseAccessor.class)
public class ConnectAddonLicenseAccessorFactory implements ServiceFactory<ConnectAddonLicenseAccessor>
{
    public static final String ATLASSIAN_PLUGIN_KEY = "Atlassian-Plugin-Key";
    public final static String TEMPO_PLUGIN_KEY = "com.tempoplugin.tempo-core";

    private final LicenseRetriever licenseRetriever;

    @Autowired
    public ConnectAddonLicenseAccessorFactory(final LicenseRetriever licenseRetriever)
    {
        this.licenseRetriever = licenseRetriever;
    }

    @Override
    public ConnectAddonLicenseAccessor getService(final Bundle bundle, final ServiceRegistration serviceRegistration)
    {
        final String pluginKey = bundle.getHeaders().get(ATLASSIAN_PLUGIN_KEY);
        if (pluginKey != null && pluginKey.equals(TEMPO_PLUGIN_KEY))
        {
            return new ConnectAddonLicenseAccessorImpl(licenseRetriever);
        }
        else
        {
            throw new UnauthorizedPluginException(pluginKey);
        }
    }

    @Override
    public void ungetService(final Bundle bundle, final ServiceRegistration serviceRegistration, final ConnectAddonLicenseAccessor o)
    {
    }

    @VisibleForTesting
    static class UnauthorizedPluginException extends RuntimeException
    {
        public UnauthorizedPluginException(final String pluginKey)
        {
            super("Plugin with key " + pluginKey + " is not authorised to access this service: " + ConnectAddonLicenseAccessor.class.getName());
        }
    }

    private static class ConnectAddonLicenseAccessorImpl implements ConnectAddonLicenseAccessor
    {
        private final LicenseRetriever licenseRetriever;

        private ConnectAddonLicenseAccessorImpl(final LicenseRetriever licenseRetriever)
        {
            this.licenseRetriever = licenseRetriever;
        }

        @Override
        public LicenseStatus getLicenseStatus(final String addonKey)
        {
            return licenseRetriever.getLicenseStatus(addonKey);
        }
    }
}
