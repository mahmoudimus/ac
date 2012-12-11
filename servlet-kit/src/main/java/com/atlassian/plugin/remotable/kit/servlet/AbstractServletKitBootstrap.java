package com.atlassian.plugin.remotable.kit.servlet;

import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.remotable.api.service.HttpResourceMounter;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.kit.servlet.internal.MultipageServlet;
import com.atlassian.plugin.remotable.kit.servlet.internal.spring.SpringServletKitBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import javax.servlet.http.HttpServlet;
import java.util.Map;

import static com.atlassian.plugin.remotable.spi.util.Strings.decapitalize;
import static com.atlassian.plugin.remotable.spi.util.Strings.removeSuffix;

/**
 *
 */
public abstract class AbstractServletKitBootstrap
{
    private static final Logger log = LoggerFactory.getLogger(SpringServletKitBootstrap.class);

    protected void register(HttpResourceMounter httpResourceMounter,
                            PluginRetrievalService pluginRetrievalService,
                            SignedRequestHandler signedRequestHandler,
                            Map<Class<? extends HttpServlet>, Provider<? extends HttpServlet>> providers
    )
    {
        for (Map.Entry<Class<? extends HttpServlet>, Provider<? extends HttpServlet>> entry : providers.entrySet())
        {
            Class<? extends HttpServlet> servletClass = entry.getKey();   
            String path;
            AppUrl appUrl = servletClass.getAnnotation(AppUrl.class);
            if (appUrl != null)
            {
                path = appUrl.value();
                if (path.charAt(0) != '/') path = "/" + path;
            }
            else
            {
                String className = servletClass.getSimpleName();
                path = "/" + decapitalize(removeSuffix(className, "Servlet"));
            }
            log.info("Found servlet '" + path + "' class '" + servletClass);

            Multipage multipage = servletClass.getAnnotation(Multipage.class);
            if (multipage != null)
            {
                String internalPath = "/internal" + path;
                String pluginKey = pluginRetrievalService.getPlugin().getKey();
                String hostBaseUrl = signedRequestHandler.getHostBaseUrl(pluginKey);
                String internalUrl = httpResourceMounter.getLocalMountBaseUrl() + internalPath;
                MultipageServlet multipageServlet = new MultipageServlet(internalUrl, hostBaseUrl);
                httpResourceMounter.mountServlet(multipageServlet, path, path + "/*");
                path = internalPath;
            }
            httpResourceMounter.mountServlet(LazyHttpServlet.create(entry.getValue()), path, path + "/*");
        }

        httpResourceMounter.mountStaticResources("", "/public/*");
    }
}
