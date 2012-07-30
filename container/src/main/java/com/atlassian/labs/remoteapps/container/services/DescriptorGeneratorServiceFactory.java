package com.atlassian.labs.remoteapps.container.services;

import com.atlassian.labs.remoteapps.container.HttpServer;
import com.atlassian.labs.remoteapps.container.internal.EnvironmentImplFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates the descriptor generator for the bundle
 */
public class DescriptorGeneratorServiceFactory implements ServiceFactory
{
    private final PluginAccessor pluginAccessor;
    private final HttpServer httpServer;
    private final OAuthSignedRequestHandlerServiceFactory oAuthSignedRequestHandlerServiceFactory;
    private final EnvironmentImplFactory environmentImplServiceFactory;
    private static final Logger log = LoggerFactory.getLogger(
            DescriptorGeneratorServiceFactory.class);

    public DescriptorGeneratorServiceFactory(PluginAccessor pluginAccessor, HttpServer httpServer,
            OAuthSignedRequestHandlerServiceFactory oAuthSignedRequestHandlerServiceFactory,
            EnvironmentImplFactory environmentImplServiceFactory)
    {
        this.pluginAccessor = pluginAccessor;
        this.httpServer = httpServer;
        this.environmentImplServiceFactory = environmentImplServiceFactory;
        this.oAuthSignedRequestHandlerServiceFactory = oAuthSignedRequestHandlerServiceFactory;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        String appKey = OsgiHeaderUtil.getPluginKey(bundle);
        Plugin plugin = pluginAccessor.getPlugin(appKey);
        return new DescriptorGeneratorLoader(plugin, httpServer,
                oAuthSignedRequestHandlerServiceFactory.getService(appKey),
                environmentImplServiceFactory.getService(appKey));
    }


    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
        // nothing for now
    }
}
