package com.atlassian.labs.remoteapps.api.services.http;

import com.atlassian.labs.remoteapps.api.services.ServiceWrappers;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class SyncHostHttpClientServiceFactory implements ServiceFactory
{
    private HostHttpClientServiceFactory asyncFactory;

    public SyncHostHttpClientServiceFactory(HostHttpClientServiceFactory asyncFactory)
    {
        this.asyncFactory = asyncFactory;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration serviceRegistration)
    {
        return getService(bundle);
    }

    public SyncHostHttpClient getService(Bundle bundle)
    {
        HostHttpClient asyncDelegate = asyncFactory.getService(bundle);
        return ServiceWrappers.wrapAsSync(asyncDelegate, SyncHostHttpClient.class);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object o)
    {
    }
}
