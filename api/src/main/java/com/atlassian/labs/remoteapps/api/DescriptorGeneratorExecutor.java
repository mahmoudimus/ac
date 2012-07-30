package com.atlassian.labs.remoteapps.api;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.osgi.framework.Bundle;
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
        descriptorGenerator.init(new DocumentRemoteAppDescriptorAccessor(bundleContext.getBundle()));
    }

    public static class DocumentRemoteAppDescriptorAccessor implements RemoteAppDescriptorAccessor
    {
        Document source;
        URL sourceUrl;
        public DocumentRemoteAppDescriptorAccessor(Bundle bundle)
        {
            try
            {
                sourceUrl = bundle.getEntry("atlassian-remote-app.xml");
                source = XmlUtils.createSecureSaxReader().read(sourceUrl);
            }
            catch (DocumentException e)
            {
                throw new IllegalArgumentException("Unable to parse generated descriptor", e);
            }
        }

        @Override
        public Document getDescriptor()
        {
            return source;
        }

        @Override
        public String getKey()
        {
            return source.getRootElement().attributeValue("key");
        }

        @Override
        public URL getDescriptorUrl()
        {
            return sourceUrl;
        }
    }
}
