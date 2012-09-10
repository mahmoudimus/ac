package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluenceBlogClient;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;

/**
 */
public class ConfluenceBlogClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluenceBlogClient>
{
    public ConfluenceBlogClientServiceFactory(
            HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory)
    {
        super(ConfluenceBlogClient.class, hostXmlRpcClientServiceFactory);
    }
}
