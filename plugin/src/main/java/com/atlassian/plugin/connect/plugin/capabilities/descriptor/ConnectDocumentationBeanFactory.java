package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.confluence.util.i18n.DocumentationBean;
import com.atlassian.confluence.util.i18n.DocumentationBeanFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class ConnectDocumentationBeanFactory implements DocumentationBeanFactory
{
    private static final Logger log = LoggerFactory.getLogger(ConnectDocumentationBeanFactory.class);

    private final AbsoluteAddOnUrlConverter urlConverter;
    private final String pluginKey;

    public ConnectDocumentationBeanFactory(AbsoluteAddOnUrlConverter urlConverter, String pluginKey)
    {
        this.urlConverter = urlConverter;
        this.pluginKey = pluginKey;
    }

    @Override
    public DocumentationBean getDocumentationBean()
    {
        return new ConnectDocumentationBean();
    }

    private class ConnectDocumentationBean implements DocumentationBean
    {
        @Override
        public String getLink(String docLink)
        {
            try
            {
                return urlConverter.getAbsoluteUrl(pluginKey, docLink);
            }
            catch (URISyntaxException e)
            {
                logError(docLink);
                return docLink;
            }
        }

        @Override
        public String getTitle(String docLink)
        {
            return "";
        }

        @Override
        public String getAlt(String docLink)
        {
            return "";
        }

        @Override
        public boolean isLocal(String docLink)
        {
            try
            {
                URI uri = new URI(docLink);
                return !uri.isAbsolute();
            }
            catch (URISyntaxException e)
            {
                logError(docLink);
                return false;
            }
        }

        private void logError(String docLink)
        {
            // help vendors find errors in their descriptors
            log.error("Malformed documentation link declared by '"
                    + pluginKey + "': "
                    + docLink);
        }

        @Override
        public boolean exists(String docLink)
        {
            return true;
        }
    }
}
