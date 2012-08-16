package com.atlassian.labs.remoteapps.api.services.http.impl;

import com.atlassian.labs.remoteapps.api.services.ServiceWrappers;
import com.atlassian.labs.remoteapps.api.services.http.HostHttpClient;
import com.atlassian.labs.remoteapps.api.services.http.SyncHostHttpClient;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class SyncHostHttpClientServiceFactory implements ServiceFactory
{
    private HostHttpClientServiceFactory hostHttpClientServiceFactory;

    public SyncHostHttpClientServiceFactory(HostHttpClientServiceFactory hostHttpClientServiceFactory)
    {
        this.hostHttpClientServiceFactory = hostHttpClientServiceFactory;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration serviceRegistration)
    {
        return getService(bundle);
    }

    public SyncHostHttpClient getService(Bundle bundle)
    {
        HostHttpClient asyncDelegate = hostHttpClientServiceFactory.getService(bundle);
        return ServiceWrappers.wrapAsSync(asyncDelegate, SyncHostHttpClient.class);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object o)
    {
    }
}
