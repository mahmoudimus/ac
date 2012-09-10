package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluenceUserClient;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;

/**
 */
public class ConfluenceUserClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluenceUserClient>
{
    public ConfluenceUserClientServiceFactory(
            HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory)
    {
        super(ConfluenceUserClient.class, hostXmlRpcClientServiceFactory);
    }
}
