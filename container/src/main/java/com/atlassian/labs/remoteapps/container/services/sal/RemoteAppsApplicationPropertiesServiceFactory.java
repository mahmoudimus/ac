package com.atlassian.labs.remoteapps.container.services.sal;

import com.atlassian.labs.remoteapps.container.HttpServer;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
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
        return new RemoteAppsApplicationProperties(httpServer.getLocalMountBaseUrl(appKey));
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
    }
}
