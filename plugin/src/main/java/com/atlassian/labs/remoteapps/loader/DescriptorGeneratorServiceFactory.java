package com.atlassian.labs.remoteapps.loader;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.api.services.RequestContext;
import com.atlassian.labs.remoteapps.api.services.RequestContextServiceFactory;
import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import com.atlassian.labs.remoteapps.loader.universalbinary.UBDispatchFilter;
import com.atlassian.labs.remoteapps.services.LocalSignedRequestHandlerServiceFactory;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads the remote app when its service is requested.  The service itself does nothing.
 */
public class DescriptorGeneratorServiceFactory implements ServiceFactory
{
    private final RemoteAppLoader remoteAppLoader;
    private final EventPublisher eventPublisher;
    private final PluginAccessor pluginAccessor;
    private final UBDispatchFilter httpResourceFilter;
    private final LocalSignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory;
    private final RequestContextServiceFactory requestContextServiceFactory;
    private static final Logger log = LoggerFactory.getLogger(DescriptorGeneratorServiceFactory.class);

    public DescriptorGeneratorServiceFactory(RemoteAppLoader remoteAppLoader,
            EventPublisher eventPublisher, PluginAccessor pluginAccessor,
            UBDispatchFilter httpResourceFilter,
            LocalSignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory,
            RequestContextServiceFactory requestContextServiceFactory)
    {
        this.remoteAppLoader = remoteAppLoader;
        this.eventPublisher = eventPublisher;
        this.pluginAccessor = pluginAccessor;
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
        return new DescriptorGeneratorLoader(bundle, remoteAppLoader, pluginAccessor, eventPublisher,
            httpResourceFilter, signedRequestHandler, requestContext);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
        remoteAppLoader.unload(bundle);
    }
}
