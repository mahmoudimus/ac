package com.atlassian.plugin.connect.jira.api;

import com.atlassian.plugin.connect.api.ConnectAddonAccessorMigrationApi;
import com.atlassian.plugin.connect.plugin.api.LicenseStatus;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
@ExportAsService
public class ConnectAddonAccessorForMigrationFactory implements ServiceFactory
{
    public static final String ATLASSIAN_PLUGIN_KEY = "Atlassian-Plugin-Key";
    public final static String TEMPO_PLUGIN_KEY = "com.tempoplugin.tempo-core";

    private final LicenseRetriever licenseRetriever;

    @Autowired
    public ConnectAddonAccessorForMigrationFactory(final LicenseRetriever licenseRetriever)
    {
        this.licenseRetriever = licenseRetriever;
    }

    @Override
    public Object getService(final Bundle bundle, final ServiceRegistration serviceRegistration)
    {
        final String pluginKey = bundle.getHeaders().get(ATLASSIAN_PLUGIN_KEY);
        if (pluginKey != null && pluginKey.equals(TEMPO_PLUGIN_KEY))
        {
            return new ConnectAddonAccessorForMigrationImpl(licenseRetriever);
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

    private static class ConnectAddonAccessorForMigrationImpl implements ConnectAddonAccessorMigrationApi
    {
        private final LicenseRetriever licenseRetriever;

        private ConnectAddonAccessorForMigrationImpl(final LicenseRetriever licenseRetriever)
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
