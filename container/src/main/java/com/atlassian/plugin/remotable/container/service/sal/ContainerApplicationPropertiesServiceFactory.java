package com.atlassian.plugin.remotable.container.service.sal;

import com.atlassian.plugin.remotable.container.HttpServer;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import com.atlassian.sal.api.ApplicationProperties;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * Builds an application properties tailored for the app
 */
public class ContainerApplicationPropertiesServiceFactory implements ServiceFactory
{
    private final HttpServer httpServer;

    public ContainerApplicationPropertiesServiceFactory(HttpServer httpServer)
    {
        this.httpServer = httpServer;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        String appKey = OsgiHeaderUtil.getPluginKey(bundle);
        return getService(appKey);
    }

    public ApplicationProperties getService(String pluginKey)
    {
        return new ContainerApplicationProperties(httpServer.getLocalMountBaseUrl(pluginKey));
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
    }
}
