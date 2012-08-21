package com.atlassian.labs.remoteapps.host.common.service.http;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class HostXmlRpcClientServiceFactory implements ServiceFactory
{
    private final HostHttpClientServiceFactory hostHttpClientServiceFactory;

    public HostXmlRpcClientServiceFactory(HostHttpClientServiceFactory hostHttpClientServiceFactory)
    {
        this.hostHttpClientServiceFactory = hostHttpClientServiceFactory;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        return new DefaultHostXmlRpcClient(hostHttpClientServiceFactory.getService(bundle));
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
    }
}
