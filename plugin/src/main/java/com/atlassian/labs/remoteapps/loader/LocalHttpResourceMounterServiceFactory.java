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
 * Loads the remote app when its service is requested.  The service itself does nothing.
 */
public class LocalHttpResourceMounterServiceFactory implements ServiceFactory
{
    private final RemoteAppLoader remoteAppLoader;
    private final UBDispatchFilter httpResourceFilter;
    private final LocalSignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory;
    private final RequestContextServiceFactory requestContextServiceFactory;
    private static final Logger log = LoggerFactory.getLogger(LocalHttpResourceMounterServiceFactory.class);

    public LocalHttpResourceMounterServiceFactory(RemoteAppLoader remoteAppLoader,
                                                  UBDispatchFilter httpResourceFilter,
                                                  LocalSignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory,
                                                  RequestContextServiceFactory requestContextServiceFactory
    )
    {
        this.remoteAppLoader = remoteAppLoader;
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
        remoteAppLoader.unload(bundle);
    }
}
