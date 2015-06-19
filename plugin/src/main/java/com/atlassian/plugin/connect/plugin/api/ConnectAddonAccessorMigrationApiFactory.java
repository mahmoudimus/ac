package com.atlassian.plugin.connect.plugin.api;

import com.atlassian.plugin.connect.api.ConnectAddonAccessorMigrationApi;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
@ExportAsService
public class ConnectAddonAccessorMigrationApiFactory implements ServiceFactory
{
    public static final String ATLASSIAN_PLUGIN_KEY = "Atlassian-Plugin-Key";
    public final static String TEMPO_PLUGIN_KEY = "com.tempoplugin.tempo-core";

    private final LicenseRetriever licenseRetriever;

    @Autowired
    public ConnectAddonAccessorMigrationApiFactory(final LicenseRetriever licenseRetriever)
    {
        this.licenseRetriever = licenseRetriever;
    }

    @Override
    public Object getService(final Bundle bundle, final ServiceRegistration serviceRegistration)
    {
        final String pluginKey = bundle.getHeaders().get(ATLASSIAN_PLUGIN_KEY);
        if (pluginKey != null && pluginKey.equals(TEMPO_PLUGIN_KEY))
        {
            return new ConnectAddonAccessorMigrationApiImpl(licenseRetriever);
        }
        else
        {
            throw new UnauthorizedPluginException(pluginKey);
        }
    }

    @Override
    public void ungetService(final Bundle bundle, final ServiceRegistration serviceRegistration, final Object o)
    {
    }

    public static class UnauthorizedPluginException extends RuntimeException
    {
        public UnauthorizedPluginException(final String pluginKey)
        {
            super("Plugin with key " + pluginKey + " is not authorised to access this service: " + ConnectAddonAccessorMigrationApi.class.getName());
        }
    }

    private static class ConnectAddonAccessorMigrationApiImpl implements ConnectAddonAccessorMigrationApi
    {
        private final LicenseRetriever licenseRetriever;

        private ConnectAddonAccessorMigrationApiImpl(final LicenseRetriever licenseRetriever)
        {
            this.licenseRetriever = licenseRetriever;
        }

        @Override
        public LicenseStatus getLicenseStatus(final String addonKey)
        {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
