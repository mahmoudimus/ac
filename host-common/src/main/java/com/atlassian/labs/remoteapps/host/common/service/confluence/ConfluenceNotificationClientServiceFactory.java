package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluenceNotificationClient;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;

/**
 */
public class ConfluenceNotificationClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluenceNotificationClient>
{
    public ConfluenceNotificationClientServiceFactory(
            HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory)
    {
        super(ConfluenceNotificationClient.class, hostXmlRpcClientServiceFactory);
    }
}
