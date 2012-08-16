package com.atlassian.labs.remoteapps.loader;

import com.atlassian.labs.remoteapps.api.services.RequestContext;
import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import com.atlassian.labs.remoteapps.api.services.impl.RequestContextServiceFactory;
import com.atlassian.labs.remoteapps.loader.universalbinary.UBDispatchFilter;
import com.atlassian.labs.remoteapps.services.LocalSignedRequestHandlerServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * Provides an http resource mounter specific to a plugin
 */
public class LocalHttpResourceMounterServiceFactory implements ServiceFactory
{
    private final UBDispatchFilter httpResourceFilter;
    private final LocalSignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory;
    private final RequestContextServiceFactory requestContextServiceFactory;

    public LocalHttpResourceMounterServiceFactory(UBDispatchFilter httpResourceFilter,
                                                  LocalSignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory,
                                                  RequestContextServiceFactory requestContextServiceFactory
    )
    {
        this.httpResourceFilter = httpResourceFilter;
        this.signedRequestHandlerServiceFactory = signedRequestHandlerServiceFactory;
        this.requestContextServiceFactory = requestContextServiceFactory;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        SignedRequestHandler signedRequestHandler = signedRequestHandlerServiceFactory.getService(bundle);
        RequestContext requestContext = requestContextServiceFactory.getService(bundle);
        return new LocalHttpResourceMounter(bundle, httpResourceFilter, signedRequestHandler, requestContext);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
    }
}
