package com.atlassian.plugin.remotable.container.service;

import com.atlassian.plugin.remotable.api.service.HttpResourceMounter;
import com.atlassian.plugin.remotable.api.service.RequestContext;
import com.atlassian.plugin.remotable.container.HttpServer;
import com.atlassian.plugin.remotable.container.internal.Environment;
import com.atlassian.plugin.remotable.container.internal.kit.RegistrationFilter;
import com.atlassian.plugin.remotable.host.common.descriptor.DelegatingDescriptorAccessor;
import com.atlassian.plugin.remotable.host.common.descriptor.DescriptorAccessor;
import com.atlassian.plugin.remotable.host.common.descriptor.DisplayUrlTransformingDescriptorAccessor;
import com.atlassian.plugin.remotable.host.common.descriptor.PolyglotDescriptorAccessor;
import com.atlassian.plugin.remotable.host.common.service.AuthenticationFilter;
import com.atlassian.plugin.Plugin;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import java.io.File;

import static com.atlassian.plugin.remotable.host.common.descriptor.DescriptorUtils.*;
import static com.google.common.base.Preconditions.*;
import static java.util.Arrays.*;

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
        this.httpServer = checkNotNull(httpServer);
        this.plugin = checkNotNull(plugin);

        final DescriptorAccessor descriptorAccessor = new DisplayUrlTransformingDescriptorAccessor(new LazyDescriptorAccessor(plugin, bundle), this.httpServer);
        environment.setEnv("BASE_URL", checkNotNull(getDisplayUrl(descriptorAccessor.getDescriptor())));
        final String oauthPublicKey = getOAuthPublicKey(descriptorAccessor.getDescriptor());
        if (oauthPublicKey != null)
        {
            environment.setEnv("OAUTH_LOCAL_PUBLIC_KEY", oauthPublicKey);
        }

        mountFilter(new RegistrationFilter(descriptorAccessor, environment, oAuthSignedRequestHandler), "/");
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

    private static final class LazyDescriptorAccessor extends DelegatingDescriptorAccessor
    {
        private final Plugin plugin;
        private final Bundle bundle;

        private LazyDescriptorAccessor(Plugin plugin, Bundle bundle)
        {
            this.plugin = plugin;
            this.bundle = bundle;
        }

        @Override
        protected DescriptorAccessor getDelegate()
        {
            DescriptorAccessor descriptorAccessor = null;
            final String localDirectories = System.getProperty("plugin.resource.directories");
            if (localDirectories != null)
            {
                String[] dirs = localDirectories.split(",");
                switch (dirs.length)
                {
                    case 1 : descriptorAccessor = new PolyglotDescriptorAccessor(new File(dirs[0]));
                        break;
                    case 0 : log.warn("System property plugin.resource.directories set but no value found");
                        break;
                    default: log.warn("More than one value in the system property plugin.resource.directories, so don't know which one to use");
                        break;
                }
            }

            if (descriptorAccessor == null)
            {
                log.debug("Loading descriptor from the bundle for plugin {}", plugin.getKey());
                descriptorAccessor = new PolyglotDescriptorAccessor(bundle);
            }

            return descriptorAccessor;
        }
    }

    private static final class EmptyHttpServlet extends HttpServlet {}
}
