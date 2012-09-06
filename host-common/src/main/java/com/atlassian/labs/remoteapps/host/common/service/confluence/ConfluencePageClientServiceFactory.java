package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluencePageClient;
import com.atlassian.labs.remoteapps.host.common.service.TypedServiceFactory;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 */
public class ConfluencePageClientServiceFactory implements TypedServiceFactory<ConfluencePageClient>
{
    private final HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory;

    public ConfluencePageClientServiceFactory(HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory)
    {
        this.hostXmlRpcClientServiceFactory = hostXmlRpcClientServiceFactory;
    }

    @Override
    public ConfluencePageClient getService(Bundle bundle)
    {
        return hostXmlRpcClientServiceFactory.getService(bundle).bind(ConfluencePageClient.class);
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        return getService(bundle);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
    }
}
