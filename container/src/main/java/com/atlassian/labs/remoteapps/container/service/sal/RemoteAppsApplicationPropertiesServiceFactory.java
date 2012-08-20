package com.atlassian.labs.remoteapps.container.service.sal;

import com.atlassian.labs.remoteapps.container.HttpServer;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import com.atlassian.sal.api.ApplicationProperties;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * Builds an application properties tailored for the app
 */
public class RemoteAppsApplicationPropertiesServiceFactory implements ServiceFactory
{
    private final HttpServer httpServer;

    public RemoteAppsApplicationPropertiesServiceFactory(HttpServer httpServer)
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
        return new RemoteAppsApplicationProperties(httpServer.getLocalMountBaseUrl(pluginKey));
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
    }
}
