package services;


import com.atlassian.labs.remoteapps.api.service.SignedRequestHandler;
import com.atlassian.labs.remoteapps.api.service.http.HostHttpClient;
import com.atlassian.labs.remoteapps.api.service.http.HostXmlRpcClient;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ServiceAccessor
{
    private static SignedRequestHandler signedRequestHandler;
    private static HostXmlRpcClient hostXmlRpcClient;
    private static HostHttpClient hostHttpClient;

    @Inject
    public ServiceAccessor(SignedRequestHandler signedRequestHandler, HostXmlRpcClient hostXmlRpcClient,
            HostHttpClient hostHttpClient)
    {
        ServiceAccessor.signedRequestHandler = signedRequestHandler;
        ServiceAccessor.hostXmlRpcClient = hostXmlRpcClient;
        ServiceAccessor.hostHttpClient = hostHttpClient;
    }

    public static SignedRequestHandler getSignedRequestHandler()
    {
        return signedRequestHandler;
    }

    public static HostXmlRpcClient getHostXmlRpcClient()
    {
        return hostXmlRpcClient;
    }

    public static HostHttpClient getHostHttpClient()
    {
        return hostHttpClient;
    }
}
