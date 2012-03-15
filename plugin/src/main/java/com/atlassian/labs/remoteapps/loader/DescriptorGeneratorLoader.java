package com.atlassian.labs.remoteapps.loader;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.api.DescriptorGenerator;
import com.atlassian.labs.remoteapps.event.RemoteAppStartFailedEvent;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import org.dom4j.Document;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kicks off the descriptor generator and sends failure events
 */
public class DescriptorGeneratorLoader implements DescriptorGenerator
{
    private final Bundle bundle;
    private final RemoteAppLoader remoteAppLoader;
    private final PluginAccessor pluginAccessor;
    private final EventPublisher eventPublisher;
    private static final Logger log = LoggerFactory.getLogger(DescriptorGeneratorLoader.class);

    public DescriptorGeneratorLoader(Bundle bundle, RemoteAppLoader remoteAppLoader,
            PluginAccessor pluginAccessor, EventPublisher eventPublisher)
    {
        this.bundle = bundle;
        this.remoteAppLoader = remoteAppLoader;
        this.pluginAccessor = pluginAccessor;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void init(Document document) throws Exception
    {
        try
        {
            remoteAppLoader.load(bundle, document);
        }
        catch (final Exception e)
        {
            final Plugin plugin = pluginAccessor.getPlugin(OsgiHeaderUtil.getPluginKey(bundle));
            eventPublisher.publish(new RemoteAppStartFailedEvent(plugin.getKey(), e));
            log.info("Remote app '{}' failed to start: {}", plugin.getKey(), e.getMessage());
            throw e;
        }
    }
}
