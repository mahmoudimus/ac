package com.atlassian.labs.remoteapps.plugin.service;

import com.atlassian.labs.remoteapps.api.service.SignedRequestHandler;
import com.atlassian.labs.remoteapps.host.common.service.SignedRequestHandlerServiceFactory;
import com.atlassian.labs.remoteapps.plugin.loader.universalbinary.UBDispatchFilter;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

import java.util.concurrent.ExecutionException;

/**
 * Creates and caches an instance per plugin
 */
public class LocalSignedRequestHandlerServiceFactory implements SignedRequestHandlerServiceFactory
{
    private final Cache<String, LocalSignedRequestHandler> instances;

    public LocalSignedRequestHandlerServiceFactory(final UBDispatchFilter ubDispatchFilter,
            final ApplicationProperties applicationProperties,
            final ConsumerService consumerService)
    {
        this.instances = CacheBuilder.newBuilder().weakValues().build(
                new CacheLoader<String, LocalSignedRequestHandler>()
                {
                    @Override
                    public LocalSignedRequestHandler load(String key) throws Exception
                    {
                        return new LocalSignedRequestHandler(ubDispatchFilter, applicationProperties,
                                consumerService, key);
                    }
                });
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        return getService(bundle);
    }

    public SignedRequestHandler getService(Bundle bundle)
    {
        String appKey = OsgiHeaderUtil.getPluginKey(bundle);
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
