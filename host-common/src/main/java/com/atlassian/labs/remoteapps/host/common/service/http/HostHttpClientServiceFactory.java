package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.api.service.RequestContext;
import com.atlassian.labs.remoteapps.api.service.SignedRequestHandler;
import com.atlassian.labs.remoteapps.api.service.http.AsyncHttpClient;
import com.atlassian.labs.remoteapps.api.service.http.HostHttpClient;
import com.atlassian.labs.remoteapps.host.common.service.RequestContextServiceFactory;
import com.atlassian.labs.remoteapps.host.common.service.SignedRequestHandlerServiceFactory;
import com.atlassian.labs.remoteapps.host.common.service.TypedServiceFactory;
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
