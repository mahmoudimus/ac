package com.atlassian.labs.remoteapps.container.services;

import com.atlassian.labs.remoteapps.api.services.RequestContext;
import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import com.atlassian.labs.remoteapps.api.services.http.AsyncHttpClient;
import com.atlassian.labs.remoteapps.api.services.http.HostHttpClient;
import com.atlassian.labs.remoteapps.api.services.http.HostHttpClientServiceFactory;
import com.atlassian.labs.remoteapps.api.services.RequestContextServiceFactory;
import com.atlassian.labs.remoteapps.api.services.http.impl.DefaultHostHttpClient;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

public class ContainerHostHttpClientServiceFactory implements HostHttpClientServiceFactory
{
    private final AsyncHttpClient asyncHttpClient;
    private RequestContextServiceFactory requestContextServiceFactory;
    private final OAuthSignedRequestHandlerServiceFactory oAuthSignedRequestHandlerServiceFactory;

    public ContainerHostHttpClientServiceFactory(AsyncHttpClient asyncHttpClient,
         RequestContextServiceFactory requestContextServiceFactory,
         OAuthSignedRequestHandlerServiceFactory oAuthSignedRequestHandlerServiceFactory)
    {
        this.asyncHttpClient = asyncHttpClient;
        this.requestContextServiceFactory = requestContextServiceFactory;
        this.oAuthSignedRequestHandlerServiceFactory = oAuthSignedRequestHandlerServiceFactory;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration serviceRegistration)
    {
        return getService(bundle);
    }

    public HostHttpClient getService(Bundle bundle)
    {
        RequestContext requestContext = requestContextServiceFactory.getService(bundle);
        SignedRequestHandler signedRequestHandler = oAuthSignedRequestHandlerServiceFactory.getService(bundle);
        return new DefaultHostHttpClient(asyncHttpClient, requestContext, signedRequestHandler);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object o)
    {
    }
}
