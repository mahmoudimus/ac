package com.atlassian.labs.remoteapps.container.services;

import com.atlassian.labs.remoteapps.container.HttpServer;
import com.atlassian.labs.remoteapps.container.internal.Environment;
import com.atlassian.labs.remoteapps.container.internal.EnvironmentImplFactory;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 7/27/12 Time: 2:20 PM To change this template use
 * File | Settings | File Templates.
 */
public class OAuthSignedRequestHandlerServiceFactory implements ServiceFactory
{
    private final Cache<String, ContainerOAuthSignedRequestHandler> instances;

    public OAuthSignedRequestHandlerServiceFactory(
            final EnvironmentImplFactory environmentImplServiceFactory, final HttpServer httpServer)
    {
        this.instances = CacheBuilder.newBuilder().weakValues().build(new CacheLoader<String, ContainerOAuthSignedRequestHandler>()
        {
            @Override
            public ContainerOAuthSignedRequestHandler load(String key) throws Exception
            {
                Environment env = environmentImplServiceFactory.getService(key);
                final ContainerOAuthSignedRequestHandler requestHandler = new ContainerOAuthSignedRequestHandler(key, env);
                requestHandler.setLocalOauthKey(key);
                requestHandler.setLocalBaseUrlIfNull(httpServer.getLocalMountBaseUrl(key));
                return requestHandler;
            }
        });
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        String appKey = OsgiHeaderUtil.getPluginKey(bundle);
        return getService(appKey);
    }

    ContainerOAuthSignedRequestHandler getService(String appKey)
    {
        try
        {
            return instances.get(appKey);
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
