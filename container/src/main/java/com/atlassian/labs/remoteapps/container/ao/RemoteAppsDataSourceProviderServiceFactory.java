package com.atlassian.labs.remoteapps.container.ao;

import com.atlassian.labs.remoteapps.container.service.sal.RemoteAppsApplicationPropertiesServiceFactory;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public final class RemoteAppsDataSourceProviderServiceFactory implements ServiceFactory
{
    private RemoteAppsDataSourceProvider remoteAppsDataSourceProvider;
    private final RemoteAppsApplicationPropertiesServiceFactory applicationProperties;

    public RemoteAppsDataSourceProviderServiceFactory(RemoteAppsApplicationPropertiesServiceFactory applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        String pluginKey = OsgiHeaderUtil.getPluginKey(bundle);
        remoteAppsDataSourceProvider = new RemoteAppsDataSourceProvider(applicationProperties.getService(pluginKey));
        return remoteAppsDataSourceProvider;
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
        remoteAppsDataSourceProvider.destroy();
    }
}
