package com.atlassian.labs.remoteapps.loader;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.event.RemoteAppStartFailedEvent;
import com.atlassian.labs.remoteapps.loader.external.DescriptorGenerator;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.parseDocument;

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
        try
        {
            URL descriptorUrl = bundle.getEntry("atlassian-remote-app.xml");
            if (descriptorUrl == null)
            {
                throw new IllegalStateException("Cannot find remote app descriptor");
            }
            remoteAppLoader.load(bundle, parseDocument(descriptorUrl));
        }
        catch (final Exception e)
        {
            final Plugin plugin = pluginAccessor.getPlugin(OsgiHeaderUtil.getPluginKey(bundle));
            eventPublisher.publish(new RemoteAppStartFailedEvent(plugin.getKey(), e));
            log.info("Remote app '{}' failed to start: {}", plugin.getKey(), e.getMessage());
            return new DescriptorGenerator()
            {
                @Override
                public void init() throws Exception
                {
                    throw e;
                }
            };
        }

        return DescriptorGenerator.NOOP_INSTANCE;
    }
    

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
        remoteAppLoader.unload(bundle);
    }
}
