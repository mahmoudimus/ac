package com.atlassian.labs.remoteapps.container.services;

import com.atlassian.labs.remoteapps.container.HttpServer;
import com.atlassian.labs.remoteapps.container.internal.BundleKey;
import com.atlassian.labs.remoteapps.container.internal.Environment;
import com.atlassian.labs.remoteapps.container.internal.EnvironmentFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import java.util.concurrent.ExecutionException;

public final class OAuthSignedRequestHandlerServiceFactory implements ServiceFactory
{
    private final Cache<BundleKey, ContainerOAuthSignedRequestHandler> instances;

    public OAuthSignedRequestHandlerServiceFactory(
            final EnvironmentFactory environmentServiceFactory, final HttpServer httpServer)
    {
        this.instances = CacheBuilder.newBuilder().weakValues().build(new CacheLoader<BundleKey, ContainerOAuthSignedRequestHandler>()
        {
            @Override
            public ContainerOAuthSignedRequestHandler load(BundleKey key) throws Exception
            {
                Environment env = environmentServiceFactory.getService(key.bundle);
                final ContainerOAuthSignedRequestHandler requestHandler = new ContainerOAuthSignedRequestHandler(key.pluginKey, env);
                requestHandler.setLocalOauthKey(key.pluginKey);
                requestHandler.setLocalBaseUrlIfNull(httpServer.getLocalMountBaseUrl(key.pluginKey));
                return requestHandler;
            }
        });
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        return getService(bundle);
    }

    ContainerOAuthSignedRequestHandler getService(Bundle bundle)
    {
        try
        {
            return instances.get(new BundleKey(bundle));
        }
        catch (ExecutionException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
    }
}
