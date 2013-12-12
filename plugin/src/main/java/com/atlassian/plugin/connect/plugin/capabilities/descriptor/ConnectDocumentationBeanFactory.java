package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.confluence.util.i18n.DocumentationBean;
import com.atlassian.confluence.util.i18n.DocumentationBeanFactory;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.uri.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class ConnectDocumentationBeanFactory implements DocumentationBeanFactory
{
    private static final Logger log = LoggerFactory.getLogger(ConnectDocumentationBeanFactory.class);

    private final RemotablePluginAccessor remotablePluginAccessor;

    public ConnectDocumentationBeanFactory(RemotablePluginAccessor remotablePluginAccessor)
    {
        this.remotablePluginAccessor = remotablePluginAccessor;
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
            if (isLocal(docLink))
            {
                URI baseUrl = remotablePluginAccessor.getBaseUrl();
                URI docLinkURI = URI.create(docLink);
                return new UriBuilder()
                        .setScheme(baseUrl.getScheme())
                        .setAuthority(baseUrl.getAuthority())
                        .setPath(docLinkURI.getPath())
                        .setQuery(docLinkURI.getQuery())
                        .setFragment(docLinkURI.getFragment())
                        .toString();
            }
            return docLink;
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
                // help vendors find errors in their descriptors
                log.error("Malformed documentation link declared by "
                        + remotablePluginAccessor.getName()
                        + " (" + remotablePluginAccessor.getKey() + "):"
                        + docLink);
            }
            return false;
        }

        @Override
        public boolean exists(String docLink)
        {
            return true;
        }
    }
}
