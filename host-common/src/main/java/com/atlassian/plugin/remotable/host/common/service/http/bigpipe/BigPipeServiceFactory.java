package com.atlassian.plugin.remotable.host.common.service.http.bigpipe;

import com.atlassian.plugin.remotable.api.service.http.bigpipe.BigPipeManager;
import com.atlassian.plugin.remotable.host.common.service.RequestContextServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.TypedServiceFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Provides a single {@link DefaultBigPipeManager} instance per bundle, using a weak value so that if the bundle isn't using the
 * service, it'll get cleaned up.
 */
public class BigPipeServiceFactory implements TypedServiceFactory<BigPipeManager>, DisposableBean
{
    private final Cache<Bundle, BigPipeManager> instances;
    private final ScheduledExecutorService cleanupThread = DefaultBigPipeManager.createCleanupThread();


    public BigPipeServiceFactory(final RequestContextServiceFactory requestContextServiceFactory)
    {
        instances = CacheBuilder.newBuilder().weakKeys().weakValues().build(new CacheLoader<Bundle, BigPipeManager>()
        {
            @Override
            public BigPipeManager load(Bundle bundle) throws Exception
            {
                return new DefaultBigPipeManager(requestContextServiceFactory.getService(bundle), cleanupThread);
            }
        });
    }

    @Override
    public BigPipeManager getService(Bundle bundle)
    {
        return instances.getUnchecked(bundle);
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        return getService(bundle);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
        instances.cleanUp();
    }

    @Override
    public void destroy() throws Exception
    {
        instances.cleanUp();
        cleanupThread.shutdownNow();
    }
}
