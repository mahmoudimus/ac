package com.atlassian.labs.remoteapps.loader;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.PluginAccessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads the remote app when its service is requested.  The service itself does nothing.
 */
public class DescriptorGeneratorServiceFactory implements ServiceFactory
{
    private final RemoteAppLoader remoteAppLoader;
    private final EventPublisher eventPublisher;
    private final PluginAccessor pluginAccessor;
    private static final Logger log = LoggerFactory.getLogger(
            DescriptorGeneratorServiceFactory.class);

    public DescriptorGeneratorServiceFactory(RemoteAppLoader remoteAppLoader,
            EventPublisher eventPublisher, PluginAccessor pluginAccessor)
    {
        this.remoteAppLoader = remoteAppLoader;
        this.eventPublisher = eventPublisher;
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        return new DescriptorGeneratorLoader(bundle, remoteAppLoader, pluginAccessor, eventPublisher);
    }
    

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
        remoteAppLoader.unload(bundle);
    }
}
