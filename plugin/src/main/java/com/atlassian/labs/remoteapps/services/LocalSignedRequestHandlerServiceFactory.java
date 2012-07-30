package com.atlassian.labs.remoteapps.services;

import com.atlassian.labs.remoteapps.loader.universalbinary.UBDispatchFilter;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 7/27/12 Time: 3:14 PM To change this template use
 * File | Settings | File Templates.
 */
public class LocalSignedRequestHandlerServiceFactory implements ServiceFactory
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
        String appKey = OsgiHeaderUtil.getPluginKey(bundle);
        return getService(appKey);
    }

    public LocalSignedRequestHandler getService(String appKey)
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
