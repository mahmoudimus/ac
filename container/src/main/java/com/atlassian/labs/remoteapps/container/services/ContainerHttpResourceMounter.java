package com.atlassian.labs.remoteapps.container.services;

import com.atlassian.labs.remoteapps.api.DescriptorAccessor;
import com.atlassian.labs.remoteapps.api.HttpResourceMounter;
import com.atlassian.labs.remoteapps.api.PolygotDescriptorAccessor;
import com.atlassian.labs.remoteapps.api.TransformingDescriptorAccessor;
import com.atlassian.labs.remoteapps.api.services.RequestContext;
import com.atlassian.labs.remoteapps.api.services.impl.AuthenticationFilter;
import com.atlassian.labs.remoteapps.container.HttpServer;
import com.atlassian.labs.remoteapps.container.internal.Environment;
import com.atlassian.labs.remoteapps.container.internal.kit.RegistrationFilter;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import net.oauth.signature.RSA_SHA1;
import org.dom4j.Document;
import org.dom4j.Element;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;

import java.io.File;
import java.net.URL;
import java.util.Collection;

import static java.util.Arrays.asList;

/**
 * Kicks off the descriptor generator and sends failure events
 */
public final class ContainerHttpResourceMounter implements HttpResourceMounter
{
    private final HttpServer httpServer;
    private static final Logger log = LoggerFactory.getLogger(ContainerHttpResourceMounter.class);
    private final Plugin plugin;

    public ContainerHttpResourceMounter(
                                        Bundle bundle,
                                        Plugin plugin,
                                        HttpServer httpServer,
                                        final ContainerOAuthSignedRequestHandler oAuthSignedRequestHandler,
                                        Environment environment,
                                        RequestContext requestContext
    )
    {
        this.httpServer = httpServer;
        this.plugin = plugin;

        DescriptorAccessor transformedDescriptorAccessor = new TransformingDescriptorAccessor(new LazyDescriptorAccessor(plugin, bundle))
        {
            @Override
            protected Document transform(Document document)
            {
                Element root = document.getRootElement();
                Element oauth = root.element("oauth");
                if (oauth != null)
                {
                    Element publicKeyElement = oauth.element("public-key");
                    if (publicKeyElement == null)
                    {
                        publicKeyElement = oauth.addElement("public-key");
                    }
                    publicKeyElement.setText(
                            oAuthSignedRequestHandler.getLocal().getProperty(RSA_SHA1.PUBLIC_KEY).toString());
                }
                root.addAttribute("display-url", getLocalMountBaseUrl());
                return document;
            }
        };

        mountFilter(new RegistrationFilter(transformedDescriptorAccessor, environment, oAuthSignedRequestHandler), "/");
        mountServlet(new EmptyHttpServlet(), "/"); // this is so that the descriptor's filter gets picked up.

        mountFilter(new AuthenticationFilter(oAuthSignedRequestHandler, requestContext), "/*");
    }

    @Override
    public String getLocalMountBaseUrl()
    {
        return httpServer.getLocalMountBaseUrl(plugin.getKey());
    }

    @Override
    public void mountFilter(Filter filter, String... urlPatterns)
    {
        httpServer.mountFilter(plugin, filter, urlPatterns);
    }

    @Override
    public void mountServlet(HttpServlet httpServlet, String... urlPatterns)
    {
        httpServer.mountServlet(plugin, httpServlet, asList(urlPatterns));
    }

    @Override
    public void mountStaticResources(String resourcePrefix, String urlPattern)
    {
        httpServer.mountStaticResources(plugin, resourcePrefix, urlPattern);
    }


    private static class LazyDescriptorAccessor implements DescriptorAccessor
    {
        private final Plugin plugin;
        private final Bundle bundle;

        private volatile DescriptorAccessor delegate;

        private LazyDescriptorAccessor(Plugin plugin, Bundle bundle)
        {
            this.plugin = plugin;
            this.bundle = bundle;
        }

        @Override
        public Document getDescriptor()
        {
            return getDescriptorAccessor().getDescriptor();
        }

        @Override
        public String getKey()
        {
            return getDescriptorAccessor().getKey();
        }

        @Override
        public URL getDescriptorUrl()
        {
            return getDescriptorAccessor().getDescriptorUrl();
        }

        private DescriptorAccessor getDescriptorAccessor()
        {
            if (delegate == null)
            {
                delegate = loadLocalDescriptorAccessor();
            }
            return delegate;
        }

        DescriptorAccessor loadLocalDescriptorAccessor()
        {
            DescriptorAccessor descriptorAccessor = null;
            if (plugin instanceof ContainerManagedPlugin)
            {
                Collection<DescriptorAccessor> factories =((ContainerManagedPlugin) plugin).getContainerAccessor().getBeansOfType(DescriptorAccessor.class);
                if (!factories.isEmpty())
                {
                    descriptorAccessor = factories.iterator().next();
                }
            }

            if (descriptorAccessor == null)
            {
                final String localDirectories = System.getProperty("plugin.resource.directories");
                if (localDirectories != null)
                {
                    String[] dirs = localDirectories.split(",");
                    switch (dirs.length)
                    {
                        case 1 : descriptorAccessor = new PolygotDescriptorAccessor(new File(dirs[0]));
                            break;
                        case 0 : log.warn("System property plugin.resource.directories set but no value found");
                            break;
                        default: log.warn("More than one value in the system property plugin.resource.directories, so don't know which one to use");
                            break;
                    }
                }
            }

            if (descriptorAccessor == null)
            {
                log.debug("Loading descriptor from the bundle for plugin {}", plugin.getKey());
                descriptorAccessor = new PolygotDescriptorAccessor(bundle);
            }

            return descriptorAccessor;
        }


    }

    private static final class EmptyHttpServlet extends HttpServlet {}
}
