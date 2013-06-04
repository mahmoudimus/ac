package com.atlassian.plugin.remotable.plugin.loader;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import com.atlassian.plugin.remotable.api.service.RequestContext;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.api.service.http.bigpipe.BigPipeManager;
import com.atlassian.plugin.remotable.host.common.service.RequestContextServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.bigpipe.BigPipeServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.bigpipe.DefaultBigPipeManager;
import com.atlassian.plugin.remotable.plugin.loader.universalbinary.UBDispatchFilter;
import com.atlassian.plugin.remotable.plugin.service.LocalSignedRequestHandlerServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.io.internal.OsgiHeaderUtils;

/**
 * Provides an http resource mounter specific to a plugin
 */
public class LocalHttpResourceMounterServiceFactory implements ServiceFactory
{
    private final UBDispatchFilter httpResourceFilter;
    private final LocalSignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory;
    private final RequestContextServiceFactory requestContextServiceFactory;
    private final BigPipeServiceFactory bigPipeServiceFactory;
    private final PluginAccessor pluginAccessor;

    public LocalHttpResourceMounterServiceFactory(UBDispatchFilter httpResourceFilter,
            LocalSignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory,
            RequestContextServiceFactory requestContextServiceFactory,
            BigPipeServiceFactory bigPipeServiceFactory, PluginAccessor pluginAccessor)
    {
        this.httpResourceFilter = httpResourceFilter;
        this.signedRequestHandlerServiceFactory = signedRequestHandlerServiceFactory;
        this.requestContextServiceFactory = requestContextServiceFactory;
        this.bigPipeServiceFactory = bigPipeServiceFactory;
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        Plugin plugin = pluginAccessor.getPlugin(OsgiHeaderUtil.getPluginKey(bundle));
        SignedRequestHandler signedRequestHandler = signedRequestHandlerServiceFactory.getService(bundle);
        RequestContext requestContext = requestContextServiceFactory.getService(bundle);
        DefaultBigPipeManager bigPipeManager = (DefaultBigPipeManager) bigPipeServiceFactory.getService(bundle);
        return new LocalHttpResourceMounter(plugin, httpResourceFilter, signedRequestHandler, requestContext,
                bigPipeManager);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
    }
}
