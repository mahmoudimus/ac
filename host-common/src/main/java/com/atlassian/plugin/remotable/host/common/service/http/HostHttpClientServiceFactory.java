package com.atlassian.plugin.remotable.host.common.service.http;

import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.httpclient.spi.ThreadLocalContextManagers;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.plugin.remotable.host.common.service.RequestContextServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.SignedRequestHandlerServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.TypedServiceFactory;
import com.atlassian.plugin.remotable.host.common.util.ServicePromise;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

public final class HostHttpClientServiceFactory implements TypedServiceFactory<HostHttpClient>
{
    private RequestContextServiceFactory requestContextServiceFactory;
    private final SignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory;

    public HostHttpClientServiceFactory(RequestContextServiceFactory requestContextServiceFactory,
                                        SignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory)
    {
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
        ServicePromise<HttpClientFactory> promise = new ServicePromise<HttpClientFactory>(bundle.getBundleContext(), HttpClientFactory.class);
        return new DefaultHostHttpClient(promise, requestContext, signedRequestHandler, ThreadLocalContextManagers.noop());
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object o)
    {
    }
}
