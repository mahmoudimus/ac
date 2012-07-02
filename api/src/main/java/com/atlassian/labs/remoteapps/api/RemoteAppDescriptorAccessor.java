package com.atlassian.labs.remoteapps.api;

import com.atlassian.plugin.PluginParseException;
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
 *
 */
public class RemoteAppDescriptorAccessor
{
    static interface UrlProvider
    {
        URL getResource(String path);
    }

    private final UrlProvider urlProvider;

    public RemoteAppDescriptorAccessor(final Bundle bundle)
    {
        urlProvider = new UrlProvider()
        {
            @Override
            public URL getResource(String path)
            {
                return bundle.getResource(path);
            }
        };
        //todo: handle automatic reloads
    }

    public RemoteAppDescriptorAccessor(final File baseDir)
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
        //todo: handle automatic reloads
    }

    public Document getDescriptor()
    {
        Document doc = loadDescriptor(urlProvider, "atlassian-remote-app.yaml", "application/yaml", true);
        if (doc == null)
        {
            doc = loadDescriptor(urlProvider, "atlassian-remote-app.yml", "application/yaml", true);
        }
        if (doc == null)
        {
            doc = loadDescriptor(urlProvider, "atlassian-remote-app.json", "application/json", true);
        }
        if (doc == null)
        {
            doc = loadDescriptor(urlProvider, "atlassian-remote-app.js", "application/json", true);
        }
        if (doc == null)
        {
            doc = loadDescriptor(urlProvider, "atlassian-remote-app.xml", "text/xml", true);
        }

        if (doc == null)
        {
            throw new IllegalArgumentException("Missing app descriptor");
        }
        else
        {
            return doc;
        }
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
}
