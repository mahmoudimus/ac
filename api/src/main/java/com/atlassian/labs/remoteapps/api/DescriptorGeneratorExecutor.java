package com.atlassian.labs.remoteapps.api;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.osgi.framework.BundleContext;

import java.net.URL;

/**
 * Executes the descriptor generator to initialize the remote app, loading the descriptor
 * from /atlassian-remote-app.xml
 */
public class DescriptorGeneratorExecutor
{
    public DescriptorGeneratorExecutor(DescriptorGenerator descriptorGenerator,
            BundleContext bundleContext) throws Exception
    {
        URL descriptorUrl = bundleContext.getBundle().getEntry("atlassian-remote-app.xml");
        if (descriptorUrl == null)
        {
            throw new IllegalStateException("Cannot find remote app descriptor");
        }
        descriptorGenerator.init(parseDocument(descriptorUrl));
    }

    public static Document parseDocument(URL xmlUrl)
    {
        Document source;
        try
        {
            source = new SAXReader().read(xmlUrl);
        }
        catch (DocumentException e)
        {
            throw new IllegalArgumentException("Unable to parse descriptor at " + xmlUrl.toString(), e);
        }

        return source;
    }
}
