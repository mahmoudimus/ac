package com.atlassian.labs.remoteapps.loader;

import com.atlassian.labs.remoteapps.api.services.RequestContext;
import com.atlassian.labs.remoteapps.api.services.RequestContextServiceFactory;
import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import com.atlassian.labs.remoteapps.loader.universalbinary.UBDispatchFilter;
import com.atlassian.labs.remoteapps.services.LocalSignedRequestHandlerServiceFactory;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an http resource mounter specific to a plugin
 */
public class LocalHttpResourceMounterServiceFactory implements ServiceFactory
{
    private final UBDispatchFilter httpResourceFilter;
    private final LocalSignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory;
    private final RequestContextServiceFactory requestContextServiceFactory;
    private static final Logger log = LoggerFactory.getLogger(LocalHttpResourceMounterServiceFactory.class);

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
        String appKey = OsgiHeaderUtil.getPluginKey(bundle);
        SignedRequestHandler signedRequestHandler = signedRequestHandlerServiceFactory.getService(appKey);
        RequestContext requestContext = requestContextServiceFactory.getService(bundle);
        return new LocalHttpResourceMounter(bundle, httpResourceFilter, signedRequestHandler, requestContext);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
    }
}
