package com.atlassian.plugin.remotable.host.common.service;

import com.atlassian.plugin.remotable.api.service.RequestContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

public class RequestContextServiceFactory implements TypedServiceFactory<RequestContext>
{
    private SignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory;

    public RequestContextServiceFactory(SignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory)
    {
        this.signedRequestHandlerServiceFactory = signedRequestHandlerServiceFactory;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        return getService(bundle);
    }

    public DefaultRequestContext getService(Bundle bundle)
    {
        return new DefaultRequestContext(signedRequestHandlerServiceFactory.getService(bundle));
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
    }
}
