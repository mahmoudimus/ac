package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.api.service.http.HostXmlRpcClient;

/**
 */
public class HostXmlRpcClientServiceFactory extends HostHttpClientConsumerServiceFactory<HostXmlRpcClient>
{
    public HostXmlRpcClientServiceFactory(
            HostHttpClientServiceFactory hostHttpClientServiceFactory)
    {
        super(hostHttpClientServiceFactory, DefaultHostXmlRpcClient.class);
    }
}
