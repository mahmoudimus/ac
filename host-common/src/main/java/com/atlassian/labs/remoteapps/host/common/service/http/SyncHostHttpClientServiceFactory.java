package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.spi.util.ServiceWrappers;
import com.atlassian.labs.remoteapps.api.service.http.HostHttpClient;
import com.atlassian.labs.remoteapps.api.service.http.SyncHostHttpClient;
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
