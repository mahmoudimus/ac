package com.atlassian.labs.remoteapps.loader.universalbinary;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.servlet.util.DefaultPathMapper;
import com.atlassian.plugin.servlet.util.PathMapper;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.security.random.SecureRandomFactory;
import com.atlassian.util.concurrent.CopyOnWriteMap;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
@Component("ubDispatchFilter")
public class UBDispatchFilter implements DisposableBean, Filter
{
    private final PathMapper pathMapper;
    private final PathMapper staticResourceMapper;
    private final Map<String, ServletEntry> servlets;
    private static final SecureRandom random = SecureRandomFactory.newInstance();
    private final PluginAccessor pluginAccessor;
    private static final Logger log = LoggerFactory.getLogger(UBDispatchFilter.class);

    private volatile Map<String, ServletContext> servletContextsByApp;
    private volatile Map<String, ServletEntry> staticResourceServletsByApp;
    private final PluginEventManager pluginEventManager;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public UBDispatchFilter(PluginAccessor pluginAccessor,
            PluginEventManager pluginEventManager, ApplicationProperties applicationProperties)
    {
        this.pluginAccessor = pluginAccessor;
        this.pluginEventManager = pluginEventManager;
        this.applicationProperties = applicationProperties;
        this.servlets = CopyOnWriteMap.newHashMap();
        this.staticResourceServletsByApp = CopyOnWriteMap.newHashMap();
        this.pathMapper = new DefaultPathMapper();
        this.staticResourceMapper = new DefaultPathMapper();
        pluginEventManager.register(this);
    }

    public String getLocalMountBaseUrl(String appKey)
    {
        return applicationProperties.getBaseUrl() + getLocalMountBasePath(appKey);
    }

    public String getLocalMountBasePath(String appKey)
    {
        return "/app/" + appKey;
    }

    public void mountServlet(String appKey, HttpServlet httpServlet, String... urlPatterns)
    {
        final Plugin plugin = pluginAccessor.getPlugin(appKey);
        ClassLoader cl = plugin.getClassLoader();
        ServletEntry servletEntry = new ServletEntry();
        servletEntry.appKey = appKey;

        servletEntry.servlet = new DelegatingUBServlet(httpServlet, cl);
        servletEntry.paths = urlPatterns;
        servlets.put(servletEntry.key, servletEntry);

        for (String urlPattern : urlPatterns)
        {
            pathMapper.put(servletEntry.key, getLocalMountBasePath(appKey) + urlPattern);
        }
    }

    public void mountResources(String appKey, String resourcePrefix)
    {
        final Plugin plugin = pluginAccessor.getPlugin(appKey);
        ClassLoader cl = plugin.getClassLoader();
        ServletEntry entry = new ServletEntry();
        entry.servlet = new DelegatingUBServlet(
                new StaticResourceServlet(plugin, resourcePrefix),
                cl);
        entry.appKey = appKey;
        staticResourceMapper.put(appKey, getLocalMountBasePath(appKey) + "/*");
        staticResourceServletsByApp.put(appKey, entry);
    }

    @PluginEventListener
    public void unregister(PluginDisabledEvent event)
    {
        String appKey = event.getPlugin().getKey();
        for (ServletEntry entry : servlets.values())
        {
            if (appKey.equals(entry.appKey))
            {
                servlets.remove(entry.key);
                pathMapper.put(entry.key, null);
                entry.servlet.destroy();
            }
        }
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException
    {
        this.servletContextsByApp = new MapMaker().makeComputingMap(new Function<String, ServletContext>()
        {
            @Override
            public ServletContext apply(String appKey)
            {
                return new UBServletContextWrapper(
                        getLocalMountBasePath(appKey),
                        pluginAccessor.getPlugin(appKey),
                        filterConfig.getServletContext(),
                        new ConcurrentHashMap<String,Object>(),
                        Collections.<String,String>emptyMap()
                        );
            }
        });
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain
    ) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        final String uri = getUri(req);
        final String servletKey = pathMapper.get(uri);

        if (servletKey == null)
        {
            String appKey = staticResourceMapper.get(uri);
            if (appKey == null)
            {
                ((HttpServletResponse)response).sendError(404);
            }
            else
            {
                ServletEntry entry = staticResourceServletsByApp.get(appKey);
                if (!entry.initialized)
                {
                    ServletConfig servletConfig = new UBServletConfig(servletKey, Collections.<String, String>emptyMap(),
                            servletContextsByApp.get(entry.appKey));
                    entry.servlet.init(servletConfig);
                    entry.initialized = true;
                }
                entry.servlet.service(request, response);
            }
        }
        else
        {
            final HttpServlet servlet = getServlet(servletKey);
            servlet.service(request, response);
        }
    }

    private HttpServlet getServlet(String servletKey) throws ServletException
    {
        ServletEntry entry = servlets.get(servletKey);
        if (entry == null)
        {
            // stale path mapper entry
            pathMapper.put(servletKey, null);
            return null;
        }
        if (!entry.initialized)
        {
            ServletConfig servletConfig = new UBServletConfig(servletKey, Collections.<String, String>emptyMap(),
                    servletContextsByApp.get(entry.appKey));
            entry.servlet.init(servletConfig);
            entry.initialized = true;
        }
        return entry.servlet;
    }

    @Override
    public void destroy()
    {
        pluginEventManager.unregister(this);
    }

    /**
     * Gets the uri from the request.  Copied from Struts 2.1.0.
     *
     * @param request The request
     * @return The uri
     */
    private static String getUri(HttpServletRequest request)
    {
        // handle http dispatcher includes.
        String uri = (String) request
                .getAttribute("javax.servlet.include.servlet_path");
        if (uri != null)
        {
            return uri;
        }

        uri = getServletPath(request);
        if (uri != null && !"".equals(uri))
        {
            return uri;
        }

        uri = request.getRequestURI();
        return uri.substring(request.getContextPath().length());
    }

    /**
     * Retrieves the current request servlet path.
     * Deals with differences between servlet specs (2.2 vs 2.3+).
     * Copied from Struts 2.1.0.
     *
     * @param request the request
     * @return the servlet path
     */
    private static String getServletPath(HttpServletRequest request)
    {
        String servletPath = request.getServletPath();

        String requestUri = request.getRequestURI();
        // Detecting other characters that the servlet container cut off (like anything after ';')
        if (requestUri != null && servletPath != null && !requestUri.endsWith(servletPath))
        {
            int pos = requestUri.indexOf(servletPath);
            if (pos > -1)
            {
                servletPath = requestUri.substring(requestUri.indexOf(servletPath));
            }
        }

        if (null != servletPath && !"".equals(servletPath))
        {
            return servletPath;
        }

        int startIndex = request.getContextPath().equals("") ? 0 : request.getContextPath().length();
        int endIndex = request.getPathInfo() == null ? requestUri.length() : requestUri.lastIndexOf(request.getPathInfo());

        if (startIndex > endIndex)
        { // this should not happen
            endIndex = startIndex;
        }

        return requestUri.substring(startIndex, endIndex);
    }

    private static class ServletEntry
    {
        public final String key = String.valueOf(random.nextLong());
        public String appKey;
        public HttpServlet servlet;
        public String[] paths;
        public volatile boolean initialized = false;
    }

}
