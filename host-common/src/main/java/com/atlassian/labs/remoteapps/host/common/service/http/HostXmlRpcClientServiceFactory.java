package com.atlassian.labs.remoteapps.host.common.service.http;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 8/27/12 Time: 9:54 AM To change this template use
 * File | Settings | File Templates.
 */
public class HostXmlRpcClientServiceFactory extends HostHttpClientConsumerServiceFactory
{
    public HostXmlRpcClientServiceFactory(
            HostHttpClientServiceFactory hostHttpClientServiceFactory)
    {
        super(hostHttpClientServiceFactory, DefaultHostXmlRpcClient.class);
    }
}
