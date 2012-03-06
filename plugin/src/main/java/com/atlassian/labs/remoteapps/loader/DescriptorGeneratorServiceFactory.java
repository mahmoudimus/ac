package com.atlassian.labs.remoteapps.loader;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.DescriptorValidator;
import com.atlassian.labs.remoteapps.ModuleGeneratorManager;
import com.atlassian.labs.remoteapps.event.RemoteAppStartFailedEvent;
import com.atlassian.labs.remoteapps.event.RemoteAppStartedEvent;
import com.atlassian.labs.remoteapps.event.RemoteAppStoppedEvent;
import com.atlassian.labs.remoteapps.event.RemoteAppUninstalledEvent;
import com.atlassian.labs.remoteapps.loader.external.DescriptorGenerator;
import com.atlassian.labs.remoteapps.modules.DefaultRemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.applinks.ApplicationTypeModule;
import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.labs.remoteapps.util.BundleUtil;
import com.atlassian.labs.remoteapps.util.tracker.WaitableServiceTracker;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import com.google.common.base.Function;
import org.dom4j.Document;
import org.dom4j.Element;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.parseDocument;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

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
