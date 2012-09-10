package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluenceLabelClient;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;

/**
 */
public class ConfluenceLabelClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluenceLabelClient>
{
    public ConfluenceLabelClientServiceFactory(
            HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory)
    {
        super(ConfluenceLabelClient.class, hostXmlRpcClientServiceFactory);
    }
}
