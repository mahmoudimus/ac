package com.atlassian.labs.remoteapps.api.services.http.impl;

import com.atlassian.labs.remoteapps.api.services.RequestContext;
import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import com.atlassian.labs.remoteapps.api.services.http.AsyncHttpClient;
import com.atlassian.labs.remoteapps.api.services.http.HostHttpClient;
import com.atlassian.labs.remoteapps.api.services.impl.RequestContextServiceFactory;
import com.atlassian.labs.remoteapps.api.services.impl.SignedRequestHandlerServiceFactory;
import com.atlassian.labs.remoteapps.api.services.impl.TypedServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

public class HostHttpClientServiceFactory implements TypedServiceFactory<HostHttpClient>
{
    private final AsyncHttpClient asyncHttpClient;
    private RequestContextServiceFactory requestContextServiceFactory;
    private final SignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory;

    public HostHttpClientServiceFactory(AsyncHttpClient asyncHttpClient,
                                        RequestContextServiceFactory requestContextServiceFactory,
                                        SignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory)
    {
        this.asyncHttpClient = asyncHttpClient;
        this.requestContextServiceFactory = requestContextServiceFactory;
        this.signedRequestHandlerServiceFactory = signedRequestHandlerServiceFactory;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration serviceRegistration)
    {
        return getService(bundle);
    }

    public HostHttpClient getService(Bundle bundle)
    {
        RequestContext requestContext = requestContextServiceFactory.getService(bundle);
        SignedRequestHandler signedRequestHandler = signedRequestHandlerServiceFactory.getService(bundle);
        return new DefaultHostHttpClient(asyncHttpClient, requestContext, signedRequestHandler);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object o)
    {
    }
}
