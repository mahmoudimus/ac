package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.host.common.service.TypedServiceFactory;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;
import com.atlassian.plugin.util.ChainingClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import java.lang.reflect.Proxy;

/**
 * Creates confluence clients via proxies
 */
public abstract class AbstractConfluenceClientServiceFactory<T> implements ServiceFactory, TypedServiceFactory<T>
{
    private final Class<T> clientClass;
    private final HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory;

    public AbstractConfluenceClientServiceFactory(Class<T> clientClass,
            HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory)
    {
        this.clientClass = clientClass;
        this.hostXmlRpcClientServiceFactory = hostXmlRpcClientServiceFactory;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        return getService(bundle);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
    }

    @Override
    public T getService(Bundle bundle)
    {
        return (T) Proxy.newProxyInstance(
                new ChainingClassLoader(getClass().getClassLoader(), clientClass.getClassLoader()),
                new Class[]{clientClass},
                new ClientInvocationHandler("confluence2", hostXmlRpcClientServiceFactory.getService(bundle)));
    }
}
