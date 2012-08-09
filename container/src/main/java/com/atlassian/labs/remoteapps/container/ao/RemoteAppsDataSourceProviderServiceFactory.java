package com.atlassian.labs.remoteapps.container.ao;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public final class RemoteAppsDataSourceProviderServiceFactory implements ServiceFactory
{
    private RemoteAppsDataSourceProvider remoteAppsDataSourceProvider;

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        remoteAppsDataSourceProvider = new RemoteAppsDataSourceProvider();
        return remoteAppsDataSourceProvider;
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
        remoteAppsDataSourceProvider.destroy();
    }
}
