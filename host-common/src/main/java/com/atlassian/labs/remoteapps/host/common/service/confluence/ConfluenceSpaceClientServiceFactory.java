package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluenceSpaceClient;
import com.atlassian.labs.remoteapps.host.common.service.TypedServiceFactory;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 */
public class ConfluenceSpaceClientServiceFactory implements TypedServiceFactory<ConfluenceSpaceClient>
{
    private final HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory;

    public ConfluenceSpaceClientServiceFactory(HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory)
    {
        this.hostXmlRpcClientServiceFactory = hostXmlRpcClientServiceFactory;
    }

    @Override
    public ConfluenceSpaceClient getService(Bundle bundle)
    {
        return hostXmlRpcClientServiceFactory.getService(bundle).bind(ConfluenceSpaceClient.class);
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
