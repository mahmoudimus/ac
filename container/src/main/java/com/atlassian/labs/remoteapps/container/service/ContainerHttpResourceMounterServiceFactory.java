package com.atlassian.labs.remoteapps.container.service;

import com.atlassian.labs.remoteapps.host.common.service.RequestContextServiceFactory;
import com.atlassian.labs.remoteapps.container.HttpServer;
import com.atlassian.labs.remoteapps.container.internal.EnvironmentFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.util.ContextClassLoaderSwitchingUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import java.util.concurrent.Callable;

import static com.atlassian.plugin.osgi.util.OsgiHeaderUtil.getPluginKey;

/**
 * Creates the descriptor generator for the bundle
 */
public class ContainerHttpResourceMounterServiceFactory implements ServiceFactory
{
    private final PluginAccessor pluginAccessor;
    private final HttpServer httpServer;
    private final OAuthSignedRequestHandlerServiceFactory oAuthSignedRequestHandlerServiceFactory;
    private RequestContextServiceFactory requestContextServiceFactory;
    private final EnvironmentFactory environmentServiceFactory;

    public ContainerHttpResourceMounterServiceFactory(PluginAccessor pluginAccessor,
                                                      HttpServer httpServer,
                                                      OAuthSignedRequestHandlerServiceFactory oAuthSignedRequestHandlerServiceFactory,
                                                      EnvironmentFactory environmentServiceFactory,
                                                      RequestContextServiceFactory requestContextServiceFactory
    )
    {
        this.pluginAccessor = pluginAccessor;
        this.httpServer = httpServer;
        this.environmentServiceFactory = environmentServiceFactory;
        this.oAuthSignedRequestHandlerServiceFactory = oAuthSignedRequestHandlerServiceFactory;
        this.requestContextServiceFactory = requestContextServiceFactory;
    }

    @Override
    public Object getService(final Bundle bundle, ServiceRegistration registration)
    {
        final Plugin plugin = pluginAccessor.getPlugin(getPluginKey(bundle));
        try
        {
            // Swap the ccl to allow xml libraries to find their properties files.   Prevents "'UTF-8' encoding not
            // supported" errors
            return ContextClassLoaderSwitchingUtil.runInContext(getClass().getClassLoader(), new Callable<Object>()
            {
                @Override
                public Object call() throws Exception
                {
                    return new ContainerHttpResourceMounter(bundle, plugin, httpServer,
                                    oAuthSignedRequestHandlerServiceFactory.getService(bundle),
                                    environmentServiceFactory.getService(bundle),
                                    requestContextServiceFactory.getService(bundle));
                }
            });
        }
        catch (Exception e)
        {
            throw new RuntimeException("Shouldn't be thrown", e);
        }
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
        // nothing for now
    }
}
