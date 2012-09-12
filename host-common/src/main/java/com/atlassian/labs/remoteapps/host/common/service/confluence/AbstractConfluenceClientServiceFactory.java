package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.host.common.service.TypedServiceFactory;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;
import com.atlassian.labs.remoteapps.spi.permission.PermissionsReader;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import com.atlassian.plugin.util.ChainingClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.zip.CheckedInputStream;

/**
 * Creates confluence clients via proxies
 */
public abstract class AbstractConfluenceClientServiceFactory<T> implements ServiceFactory, TypedServiceFactory<T>
{
    private final Class<T> clientClass;
    private final HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory;
    private final PermissionsReader permissionsReader;
    private final PluginAccessor pluginAccessor;


    public AbstractConfluenceClientServiceFactory(Class<T> clientClass,
                                                  HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory,
                                                  PermissionsReader permissionsReader,
                                                  PluginAccessor pluginAccessor
    )
    {
        this.clientClass = clientClass;
        this.hostXmlRpcClientServiceFactory = hostXmlRpcClientServiceFactory;
        this.permissionsReader = permissionsReader;
        this.pluginAccessor = pluginAccessor;
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
        Plugin plugin = pluginAccessor.getPlugin(OsgiHeaderUtil.getPluginKey(bundle));
        if (plugin == null)
        {
            throw new IllegalArgumentException("Clients can only be used by plugins");
        }
        Set<String> permissions = permissionsReader.getPermissionsForPlugin(plugin);
        return (T) Proxy.newProxyInstance(
                new ChainingClassLoader(getClass().getClassLoader(), clientClass.getClassLoader()),
                new Class[]{clientClass},
                new ClientInvocationHandler("confluence2", hostXmlRpcClientServiceFactory.getService(bundle), permissions, plugin.getKey()));
    }
}
