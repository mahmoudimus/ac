package com.atlassian.labs.remoteapps.container.ao;

import com.atlassian.labs.remoteapps.container.internal.EnvironmentFactory;
import com.google.common.base.Preconditions;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import static com.google.common.base.Preconditions.checkNotNull;

public final class RemoteAppsDataSourceProviderServiceFactory implements ServiceFactory
{
    private final EnvironmentFactory environmentFactory;
    private RemoteAppsDataSourceProvider remoteAppsDataSourceProvider;

    public RemoteAppsDataSourceProviderServiceFactory(EnvironmentFactory environmentFactory)
    {
        this.environmentFactory = checkNotNull(environmentFactory);
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        remoteAppsDataSourceProvider = new RemoteAppsDataSourceProvider(environmentFactory.getService(bundle));
        return remoteAppsDataSourceProvider;
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
        remoteAppsDataSourceProvider.destroy();
    }
}
