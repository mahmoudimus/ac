package com.atlassian.plugin.remotable.plugin.webhook.impl;

import com.atlassian.plugin.remotable.spi.webhook.PluginUriResolver;
import com.atlassian.sal.api.ApplicationProperties;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.*;

public final class PluginUriResolverImpl implements PluginUriResolver
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ApplicationProperties applicationProperties;
    private final BundleContext bundleContext;

    public PluginUriResolverImpl(ApplicationProperties applicationProperties, BundleContext bundleContext)
    {
        this.applicationProperties = checkNotNull(applicationProperties);
        this.bundleContext = checkNotNull(bundleContext);
    }

    @Override
    public URI getUri(String pluginKey, URI path)
    {
        final URI newUri = getFromOsgiService(pluginKey, path);
        if (newUri != null)
        {
            logger.debug("Found new URI from OSGi service, '{}'", newUri);
            return newUri;
        }

        final URI defaultNewUri = getUriDefault(path);
        logger.debug("Found new URI from default Application properties, '{}'", defaultNewUri);
        return defaultNewUri;
    }

    private URI getFromOsgiService(String pluginKey, URI path)
    {
        final ServiceReference newPluginUriResolverReference = bundleContext.getServiceReference(PluginUriResolver.class.getName());
        if (newPluginUriResolverReference != null)
        {
            try
            {
                final PluginUriResolver newUriResolver = (PluginUriResolver) bundleContext.getService(newPluginUriResolverReference);
                return newUriResolver.getUri(pluginKey, path);
            }
            finally
            {
                bundleContext.ungetService(newPluginUriResolverReference);
            }
        }

        return null;
    }

    private URI getUriDefault(URI path)
    {
        final String newUri = applicationProperties.getBaseUrl() + path.toString();
        try
        {
            return new URI(newUri);
        }
        catch (URISyntaxException e)
        {
            throw new IllegalStateException("Could not parse the new URI, " + newUri, e);
        }
    }
}
