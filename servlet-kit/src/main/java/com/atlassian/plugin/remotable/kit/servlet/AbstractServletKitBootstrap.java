package com.atlassian.plugin.remotable.kit.servlet;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.remotable.api.service.HttpResourceMounter;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.kit.servlet.internal.MultipageServlet;
import com.atlassian.plugin.remotable.kit.servlet.internal.spring.SpringServletKitBootstrap;
import com.google.common.base.Supplier;
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

    protected final void register(final HttpResourceMounter httpResourceMounter,
                            Plugin plugin,
                            final SignedRequestHandler signedRequestHandler,
                            Map<Class<? extends HttpServlet>, Provider<? extends HttpServlet>> providers)
    {
        for (Map.Entry<Class<? extends HttpServlet>, Provider<? extends HttpServlet>> entry : providers.entrySet())
        {
            registerServlet(httpResourceMounter, plugin, signedRequestHandler, entry.getKey(), entry.getValue());
        }

        httpResourceMounter.mountStaticResources("", "/public/*");
    }

    private void registerServlet(final HttpResourceMounter httpResourceMounter,
                                 Plugin plugin,
                                 final SignedRequestHandler signedRequestHandler,
                                 Class<? extends HttpServlet> servletClass,
                                 Provider<? extends HttpServlet> servletProvider)
    {
        String path = getServletPath(servletClass);
        if (isMultiPageServlet(servletClass))
        {
            final String internalPath = getInternalPath(path);
            final Supplier<String> hostBaseUrl = new HostBaseUrlSupplier(signedRequestHandler, plugin.getKey());
            final Supplier<String> internalUrl = new InternalUrlSupplier(httpResourceMounter, internalPath);

            httpResourceMounter.mountServlet(new MultipageServlet(internalUrl, hostBaseUrl), path, path + "/*");
            path = internalPath;
        }
        httpResourceMounter.mountServlet(LazyHttpServlet.create(servletProvider), path, path + "/*");
    }

    private boolean isMultiPageServlet(Class<? extends HttpServlet> servletClass)
    {
        return servletClass.isAnnotationPresent(Multipage.class);
    }

    private static String getServletPath(Class<? extends HttpServlet> servletClass)
    {
        final String path;
        if (servletClass.isAnnotationPresent(AppUrl.class))
        {
            path = prependForwardSlash(servletClass.getAnnotation(AppUrl.class).value());
        }
        else
        {
            final String className = servletClass.getSimpleName();
            path = prependForwardSlash(decapitalize(removeSuffix(className, "Servlet")));
        }

        log.info("Found servlet '{}' which will be registered at '{}'", servletClass.getName(), path);
        return path;
    }

    private static String getInternalPath(String path)
    {
        return "/internal" + path;
    }

    private static String prependForwardSlash(String path)
    {
        return path.charAt(0) != '/' ? "/" + path : path;
    }

    private static final class HostBaseUrlSupplier implements Supplier<String>
    {
        private final SignedRequestHandler signedRequestHandler;
        private final String pluginKey;

        private HostBaseUrlSupplier(SignedRequestHandler signedRequestHandler, String pluginKey)
        {
            this.signedRequestHandler = signedRequestHandler;
            this.pluginKey = pluginKey;
        }

        @Override
        public String get()
        {
            return signedRequestHandler.getHostBaseUrl(pluginKey);
        }
    }

    private static final class InternalUrlSupplier implements Supplier<String>
    {
        private final HttpResourceMounter httpResourceMounter;
        private final String internalPath;

        private InternalUrlSupplier(HttpResourceMounter httpResourceMounter, String path)
        {
            this.httpResourceMounter = httpResourceMounter;
            this.internalPath = path;
        }

        @Override
        public String get()
        {
            return httpResourceMounter.getLocalMountBaseUrl() + internalPath;
        }
    }
}
