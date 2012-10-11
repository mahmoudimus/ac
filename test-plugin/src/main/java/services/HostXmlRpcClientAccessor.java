package services;


import com.atlassian.plugin.remotable.api.service.http.HostXmlRpcClient;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HostXmlRpcClientAccessor
{
    private static HostXmlRpcClient signedRequestHandler;

    @Inject
    public HostXmlRpcClientAccessor(HostXmlRpcClient signedRequestHandler)
    {
        this.signedRequestHandler = signedRequestHandler;
    }

    public static HostXmlRpcClient getHostXmlRpcClient()
    {
        return signedRequestHandler;
    }
}
