package com.atlassian.labs.remoteapps.services;

import com.atlassian.labs.remoteapps.api.services.RequestContext;
import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import com.atlassian.labs.remoteapps.api.services.http.AsyncHttpClient;
import com.atlassian.labs.remoteapps.api.services.http.HostHttpClient;
import com.atlassian.labs.remoteapps.api.services.http.HostHttpClientServiceFactory;
import com.atlassian.labs.remoteapps.api.services.RequestContextServiceFactory;
import com.atlassian.labs.remoteapps.api.services.http.impl.DefaultHostHttpClient;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

public class LocalHostHttpClientServiceFactory implements HostHttpClientServiceFactory
{
    private final AsyncHttpClient asyncHttpClient;
    private final RequestContextServiceFactory requestContextServiceFactory;
    private final LocalSignedRequestHandlerServiceFactory localSignedRequestHandlerServiceFactory;

    public LocalHostHttpClientServiceFactory(AsyncHttpClient asyncHttpClient,
                                             RequestContextServiceFactory requestContextServiceFactory,
                                             LocalSignedRequestHandlerServiceFactory localSignedRequestHandlerServiceFactory)
    {
        this.asyncHttpClient = asyncHttpClient;
        this.requestContextServiceFactory = requestContextServiceFactory;
        this.localSignedRequestHandlerServiceFactory = localSignedRequestHandlerServiceFactory;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration serviceRegistration)
    {
        return getService(bundle);
    }

    public HostHttpClient getService(Bundle bundle)
    {
        RequestContext requestContext = requestContextServiceFactory.getService(bundle);
        String appKey = OsgiHeaderUtil.getPluginKey(bundle);
        SignedRequestHandler signedRequestHandler = localSignedRequestHandlerServiceFactory.getService(appKey);
        return new DefaultHostHttpClient(asyncHttpClient, requestContext, signedRequestHandler);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object o)
    {
    }
}
