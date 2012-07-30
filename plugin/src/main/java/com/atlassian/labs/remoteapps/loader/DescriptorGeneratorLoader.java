package com.atlassian.labs.remoteapps.loader;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.api.DescriptorGenerator;
import com.atlassian.labs.remoteapps.api.RemoteAppDescriptorAccessor;
import com.atlassian.labs.remoteapps.api.TransformingRemoteAppDescriptorAccessor;
import com.atlassian.labs.remoteapps.event.RemoteAppStartFailedEvent;
import com.atlassian.labs.remoteapps.loader.universalbinary.UBDispatchFilter;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import net.oauth.signature.RSA_SHA1;
import org.dom4j.Document;
import org.dom4j.Element;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;

/**
 * Kicks off the descriptor generator and sends failure events
 */
public class DescriptorGeneratorLoader implements DescriptorGenerator
{
    private final Bundle bundle;
    private final RemoteAppLoader remoteAppLoader;
    private final PluginAccessor pluginAccessor;
    private final EventPublisher eventPublisher;
    private final UBDispatchFilter httpResourceFilter;
    private static final Logger log = LoggerFactory.getLogger(DescriptorGeneratorLoader.class);
    private final String appKey;

    public DescriptorGeneratorLoader(Bundle bundle, RemoteAppLoader remoteAppLoader,
            PluginAccessor pluginAccessor, EventPublisher eventPublisher,
            UBDispatchFilter httpResourceFilter)
    {
        this.bundle = bundle;
        this.remoteAppLoader = remoteAppLoader;
        this.pluginAccessor = pluginAccessor;
        this.eventPublisher = eventPublisher;
        this.httpResourceFilter = httpResourceFilter;
        this.appKey = OsgiHeaderUtil.getPluginKey(bundle);
    }

    @Override
    public String getLocalMountBaseUrl()
    {
        return httpResourceFilter.getLocalMountBaseUrl(appKey);
    }

    @Override
    public void init(RemoteAppDescriptorAccessor descriptorAccessor) throws Exception
    {
        Document descriptor = descriptorAccessor.getDescriptor();
        if (descriptor.getRootElement().attribute("display-url") == null)
        {
            descriptor.getRootElement().addAttribute("display-url", getLocalMountBaseUrl());
        }
        try
        {
            remoteAppLoader.load(bundle, descriptor);
        }
        catch (final Exception e)
        {
            final Plugin plugin = pluginAccessor.getPlugin(OsgiHeaderUtil.getPluginKey(bundle));
            eventPublisher.publish(new RemoteAppStartFailedEvent(plugin.getKey(), e));
            log.info("Remote app '{}' failed to start: {}", plugin.getKey(), e.getMessage());
            throw e;
        }
    }

    @Override
    public void mountFilter(Filter filter, String... urlPatterns)
    {
        httpResourceFilter.mountFilter(appKey, filter, urlPatterns);
    }

    @Override
    public void mountServlet(HttpServlet httpServlet, String... urlPatterns)
    {
        httpResourceFilter.mountServlet(appKey, httpServlet, urlPatterns);
    }

    @Override
    public void mountStaticResources(String resourcePrefix, String urlPattern)
    {
        httpResourceFilter.mountResources(appKey, resourcePrefix, urlPattern);
    }
}
