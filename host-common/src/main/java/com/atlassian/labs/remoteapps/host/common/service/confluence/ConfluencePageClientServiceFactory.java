package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluencePageClient;
import com.atlassian.labs.remoteapps.host.common.service.TypedServiceFactory;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 */
public class ConfluencePageClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluencePageClient>
{
    public ConfluencePageClientServiceFactory(HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory)
    {
        super(ConfluencePageClient.class, hostXmlRpcClientServiceFactory);
    }
}
