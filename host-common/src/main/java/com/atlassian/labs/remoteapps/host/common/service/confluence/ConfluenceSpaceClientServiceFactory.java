package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluenceSpaceClient;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;

/**
 */
public class ConfluenceSpaceClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluenceSpaceClient>
{
    public ConfluenceSpaceClientServiceFactory(
            HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory)
    {
        super(ConfluenceSpaceClient.class, hostXmlRpcClientServiceFactory);
    }
}
