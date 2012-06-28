package com.atlassian.labs.remoteapps.container.services;

import com.atlassian.labs.remoteapps.container.HttpServer;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates the descriptor generator for the bundle
 */
public class DescriptorGeneratorServiceFactory implements ServiceFactory
{
    private final PluginAccessor pluginAccessor;
    private final HttpServer httpServer;
    private static final Logger log = LoggerFactory.getLogger(
            DescriptorGeneratorServiceFactory.class);

    public DescriptorGeneratorServiceFactory(PluginAccessor pluginAccessor, HttpServer httpServer)
    {
        this.pluginAccessor = pluginAccessor;
        this.httpServer = httpServer;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        String appKey = OsgiHeaderUtil.getPluginKey(bundle);
        Plugin plugin = pluginAccessor.getPlugin(appKey);
        return new DescriptorGeneratorLoader(plugin, httpServer);
    }


    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
        // nothing for now
    }
}
