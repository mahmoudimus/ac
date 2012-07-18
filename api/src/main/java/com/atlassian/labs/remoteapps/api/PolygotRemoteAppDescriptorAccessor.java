package com.atlassian.labs.remoteapps.api;

import com.atlassian.plugin.PluginParseException;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.osgi.framework.Bundle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static com.atlassian.labs.remoteapps.api.XmlUtils.createSecureSaxReader;

/**
 * Descriptor accessor that supports json, xml, and yaml descriptors
 */
public class PolygotRemoteAppDescriptorAccessor implements RemoteAppDescriptorAccessor
{
    private static final Iterable<DescriptorType> DESCRIPTOR_TYPES = ImmutableSet.of(
            new DescriptorType("atlassian-remote-app.yaml", "application/yaml", true),
            new DescriptorType("atlassian-remote-app.yml", "application/yaml", true),
            new DescriptorType("atlassian-remote-app.json", "application/json", true),
            new DescriptorType("atlassian-remote-app.js", "application/json", true),
            new DescriptorType("atlassian-remote-app.xml", "text/xml", true)
    );
    static interface UrlProvider
    {
        URL getResource(String path);
    }

    private final UrlProvider urlProvider;
    private final DescriptorType descriptorType;

    public PolygotRemoteAppDescriptorAccessor(final Bundle bundle)
    {
        urlProvider = new UrlProvider()
        {
            @Override
            public URL getResource(String path)
            {
                return bundle.getResource(path);
            }
        };
        descriptorType = determineDescriptorType(urlProvider);
    }

    public PolygotRemoteAppDescriptorAccessor(final File baseDir)
    {
        this.urlProvider = new UrlProvider()
        {
            @Override
            public URL getResource(String path)
            {
                try
                {
                    File file = new File(baseDir, path);
                    return file.exists() ? file.toURI().toURL() : null;
                }
                catch (MalformedURLException e)
                {
                    throw new IllegalArgumentException(path);
                }
            }
        };
        descriptorType = determineDescriptorType(urlProvider);
    }

    @Override
    public Document getDescriptor()
    {
        return loadDescriptor(urlProvider, descriptorType.path, descriptorType.contentType, descriptorType.convert);
    }

    @Override
    public String getKey()
    {
        return getDescriptor().getRootElement().attributeValue("key");
    }

    @Override
    public URL getDescriptorUrl()
    {
        return urlProvider.getResource(descriptorType.path);
    }

    private DescriptorType determineDescriptorType(UrlProvider urlProvider)
    {
        for (DescriptorType type : DESCRIPTOR_TYPES)
        {
            if (urlProvider.getResource(type.path) != null)
            {
                return type;
            }
        }
        throw new IllegalArgumentException("No descriptor found");
    }

    static Document loadDescriptor(UrlProvider urlProvider, String path, String contentType, boolean convert)
    {
        URL descriptorUrl = urlProvider.getResource(path);
        if (descriptorUrl != null)
        {
            if (convert)
            {
                FormatConverter converter = new FormatConverter();
                InputStream in = null;
                try
                {
                    in = descriptorUrl.openStream();
                    String content = IOUtils.toString(in);
                    return converter.toDocument(path, contentType, content);
                }
                catch (IOException e)
                {
                    throw new IllegalArgumentException("Invalid path: " + path, e);
                }
                finally
                {
                    IOUtils.closeQuietly(in);
                }

            }
            else
            {
                try
                {
                    return createSecureSaxReader().read(descriptorUrl);
                }
                catch (DocumentException e)
                {
                    throw new PluginParseException("Unable to read and parse app descriptor", e);
                }
            }
        }
        else
        {
            return null;
        }
    }

    private static class DescriptorType
    {
        final String path;
        final String contentType;
        final boolean convert;

        private DescriptorType(String path, String contentType, boolean convert)
        {
            this.path = path;
            this.contentType = contentType;
            this.convert = convert;
        }

    }
}
