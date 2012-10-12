package com.atlassian.plugin.remotable.host.common.service.http;

import com.atlassian.httpclient.apache.httpcomponents.DefaultHttpClient;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.plugin.remotable.host.common.service.DefaultRequestContext;
import com.atlassian.plugin.remotable.host.common.service.RequestContextServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.SignedRequestHandlerServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.TypedServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

public class HostHttpClientServiceFactory implements TypedServiceFactory<HostHttpClient>
{
    private final DefaultHttpClient httpClient;
    private RequestContextServiceFactory requestContextServiceFactory;
    private final SignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory;

    public HostHttpClientServiceFactory(DefaultHttpClient httpClient,
                                        RequestContextServiceFactory requestContextServiceFactory,
                                        SignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory)
    {
        this.httpClient = httpClient;
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
        DefaultRequestContext requestContext = requestContextServiceFactory.getService(bundle);
        SignedRequestHandler signedRequestHandler = signedRequestHandlerServiceFactory.getService(bundle);
        return new DefaultHostHttpClient(httpClient, requestContext, signedRequestHandler);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object o)
    {
    }
}
