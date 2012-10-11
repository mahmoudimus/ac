package com.atlassian.plugin.remotable.host.common.service.http;

import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.plugin.remotable.host.common.service.TypedServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

import java.lang.reflect.InvocationTargetException;

/**
 * Generic service factory for classes that only need {@link HostHttpClient}
 */
public class HostHttpClientConsumerServiceFactory<T> implements TypedServiceFactory<T>
{
    private final HostHttpClientServiceFactory hostHttpClientServiceFactory;
    private final Class<? extends T> serviceClass;

    public HostHttpClientConsumerServiceFactory(
            HostHttpClientServiceFactory hostHttpClientServiceFactory, Class<? extends T> serviceClass)
    {
        this.hostHttpClientServiceFactory = hostHttpClientServiceFactory;
        this.serviceClass = serviceClass;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        return getService(bundle);
    }

    public T getService(Bundle bundle)
    {
        try
        {
            return (T) serviceClass.getConstructor(HostHttpClient.class).newInstance(hostHttpClientServiceFactory.getService(bundle));
        }
        catch (InstantiationException e)
        {
            throw new IllegalStateException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new IllegalStateException(e);
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
    }
}
