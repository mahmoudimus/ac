package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluenceAdminClient;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;

/**
 */
public class ConfluenceAdminClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluenceAdminClient>
{
    public ConfluenceAdminClientServiceFactory(
            HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory)
    {
        super(ConfluenceAdminClient.class, hostXmlRpcClientServiceFactory);
    }
}
