package com.atlassian.labs.remoteapps.container.services;

import com.atlassian.labs.remoteapps.api.services.RequestContext;
import com.atlassian.labs.remoteapps.container.HttpServer;
import com.atlassian.labs.remoteapps.container.internal.EnvironmentFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import static com.atlassian.plugin.osgi.util.OsgiHeaderUtil.*;

/**
 * Creates the descriptor generator for the bundle
 */
public class DescriptorGeneratorServiceFactory implements ServiceFactory
{
    private final PluginAccessor pluginAccessor;
    private final HttpServer httpServer;
    private final OAuthSignedRequestHandlerServiceFactory oAuthSignedRequestHandlerServiceFactory;
    private final EnvironmentFactory environmentServiceFactory;
    private RequestContext requestContext;

    public DescriptorGeneratorServiceFactory(PluginAccessor pluginAccessor, HttpServer httpServer,
            OAuthSignedRequestHandlerServiceFactory oAuthSignedRequestHandlerServiceFactory,
            EnvironmentFactory environmentServiceFactory,
            RequestContext requestContext)
    {
        this.pluginAccessor = pluginAccessor;
        this.httpServer = httpServer;
        this.environmentServiceFactory = environmentServiceFactory;
        this.oAuthSignedRequestHandlerServiceFactory = oAuthSignedRequestHandlerServiceFactory;
        this.requestContext = requestContext;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        final Plugin plugin = pluginAccessor.getPlugin(getPluginKey(bundle));
        return new DescriptorGeneratorLoader(plugin, httpServer,
                oAuthSignedRequestHandlerServiceFactory.getService(bundle),
                environmentServiceFactory.getService(bundle),
                requestContext);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
        // nothing for now
    }
}
