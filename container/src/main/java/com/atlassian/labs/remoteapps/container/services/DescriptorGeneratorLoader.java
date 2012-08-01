package com.atlassian.labs.remoteapps.container.services;

import com.atlassian.labs.remoteapps.api.DescriptorGenerator;
import com.atlassian.labs.remoteapps.api.RemoteAppDescriptorAccessor;
import com.atlassian.labs.remoteapps.api.TransformingRemoteAppDescriptorAccessor;
import com.atlassian.labs.remoteapps.api.services.RequestContext;
import com.atlassian.labs.remoteapps.api.services.impl.AuthenticationFilter;
import com.atlassian.labs.remoteapps.container.HttpServer;
import com.atlassian.labs.remoteapps.container.internal.Environment;
import com.atlassian.labs.remoteapps.container.internal.kit.RegistrationFilter;
import com.atlassian.plugin.Plugin;
import net.oauth.signature.RSA_SHA1;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;

import static java.util.Arrays.asList;

/**
 * Kicks off the descriptor generator and sends failure events
 */
public class DescriptorGeneratorLoader implements DescriptorGenerator
{
    private final HttpServer httpServer;
    private static final Logger log = LoggerFactory.getLogger(DescriptorGeneratorLoader.class);
    private final Plugin plugin;
    private final ContainerOAuthSignedRequestHandler oAuthSignedRequestHandler;
    private final Environment environment;
    private final RequestContext requestContext;

    public DescriptorGeneratorLoader(Plugin plugin, HttpServer httpServer,
                                     ContainerOAuthSignedRequestHandler oAuthSignedRequestHandler, Environment environment,
                                     RequestContext requestContext)
    {
        this.httpServer = httpServer;
        this.plugin = plugin;
        this.oAuthSignedRequestHandler = oAuthSignedRequestHandler;
        this.environment = environment;
        this.requestContext = requestContext;
    }

    @Override
    public String getLocalMountBaseUrl()
    {
        return httpServer.getLocalMountBaseUrl(plugin.getKey());
    }

    @Override
    public void init(RemoteAppDescriptorAccessor descriptorAccessor) throws Exception
    {
        RemoteAppDescriptorAccessor transformedDescriptorAccessor = new TransformingRemoteAppDescriptorAccessor(descriptorAccessor)
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
        mountFilter(new AuthenticationFilter(oAuthSignedRequestHandler, requestContext), "/*");
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
}
