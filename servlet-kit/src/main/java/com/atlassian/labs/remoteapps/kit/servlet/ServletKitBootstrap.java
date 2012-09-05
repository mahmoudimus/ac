package com.atlassian.labs.remoteapps.kit.servlet;

import com.atlassian.labs.remoteapps.api.service.HttpResourceMounter;
import com.atlassian.labs.remoteapps.api.annotation.ServiceReference;
import com.atlassian.labs.remoteapps.api.service.RequestContext;
import com.atlassian.labs.remoteapps.api.service.SignedRequestHandler;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.util.Collection;
import java.util.Locale;

/**
 *
 */
@Singleton
public class ServletKitBootstrap
{
    private static final Logger log = LoggerFactory.getLogger(ServletKitBootstrap.class);

    @Inject
    public ServletKitBootstrap(
        ApplicationContext applicationContext,
        @ServiceReference HttpResourceMounter httpResourceMounter,
        @ServiceReference PluginRetrievalService pluginRetrievalService,
        @ServiceReference RequestContext requestContext,
        @ServiceReference SignedRequestHandler signedRequestHandler)
        throws Exception
    {
        for (HttpServlet servlet : (Collection<HttpServlet>)applicationContext.getBeansOfType(Servlet.class).values())
        {
            String path;
            AppUrl appUrl = servlet.getClass().getAnnotation(AppUrl.class);
            if (appUrl != null)
            {
                path = appUrl.value();
                if (path.charAt(0) != '/') path = "/" + path;
            }
            else
            {
                String className = servlet.getClass().getSimpleName();
                path = "/" + String.valueOf(className.charAt(0)).toLowerCase(Locale.US) +
                        (className.endsWith("Servlet") ? className.substring(1, className.length() - "Servlet".length()) : className.substring(1, className.length()));
            }
            log.info("Found servlet '" + path + "' class '" + servlet.getClass());

            Multipage multipage = servlet.getClass().getAnnotation(Multipage.class);
            if (multipage != null)
            {
                String internalPath = "/internal" + path;
                String pluginKey = pluginRetrievalService.getPlugin().getKey();
                String hostBaseUrl = signedRequestHandler.getHostBaseUrl(pluginKey);
                String internalUrl = httpResourceMounter.getLocalMountBaseUrl() + internalPath;
                MultipageServlet multipageServlet = new MultipageServlet(internalUrl, hostBaseUrl, requestContext);
                httpResourceMounter.mountServlet(multipageServlet, path, path + "/*");
                path = internalPath;
            }
            httpResourceMounter.mountServlet(servlet, path, path + "/*");
        }

        httpResourceMounter.mountStaticResources("", "/public/*");
    }
}
