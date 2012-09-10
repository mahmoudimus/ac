package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluenceAttachmentClient;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;

/**
 */
public class ConfluenceAttachmentClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluenceAttachmentClient>
{
    public ConfluenceAttachmentClientServiceFactory(
            HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory)
    {
        super(ConfluenceAttachmentClient.class, hostXmlRpcClientServiceFactory);
    }
}
