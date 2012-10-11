package com.atlassian.plugin.remotable.host.common.service.http;

import com.atlassian.plugin.remotable.api.service.http.HostXmlRpcClient;

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
