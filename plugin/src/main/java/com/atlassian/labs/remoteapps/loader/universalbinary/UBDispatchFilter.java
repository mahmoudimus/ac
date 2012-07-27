package com.atlassian.labs.remoteapps.loader.universalbinary;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.servlet.filter.FilterDispatcherCondition;
import com.atlassian.plugin.servlet.filter.IteratingFilterChain;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;

/**
 *
 */
@Component("ubDispatchFilter")
public class UBDispatchFilter implements DisposableBean, Filter
{
    private final PathMapper servletPathMapper;
    private final PathMapper filterPathMapper;
    private final Map<String, DispatcherEntry<HttpServlet>> servlets;
    private final Map<String, DispatcherEntry<Filter>> filters;
    private static final SecureRandom random = SecureRandomFactory.newInstance();
    private final PluginAccessor pluginAccessor;
    private static final Logger log = LoggerFactory.getLogger(UBDispatchFilter.class);

    private volatile Map<String, ServletContext> servletContextsByApp;
    private final PluginEventManager pluginEventManager;
    private final ApplicationProperties applicationProperties;

    private final Pattern APP_KEY_FINDER = Pattern.compile("/app/([^/]*)/.*");

    @Autowired
    public UBDispatchFilter(PluginAccessor pluginAccessor,
            PluginEventManager pluginEventManager, ApplicationProperties applicationProperties)
    {
        this.pluginAccessor = pluginAccessor;
        this.pluginEventManager = pluginEventManager;
        this.applicationProperties = applicationProperties;
        this.servlets = CopyOnWriteMap.newHashMap();
        this.filters = CopyOnWriteMap.newHashMap();
        this.servletPathMapper = new DefaultPathMapper();
        this.filterPathMapper = new DefaultPathMapper();
        pluginEventManager.register(this);
    }

    public String getLocalMountBaseUrl(String appKey)
    {
        return applicationProperties.getBaseUrl() + getLocalMountBasePath(appKey);
    }

    public static String getLocalMountBasePath(String appKey)
    {
        return "/app/" + appKey;
    }

    public void mountServlet(String appKey, HttpServlet httpServlet, String... urlPatterns)
    {
        final Plugin plugin = pluginAccessor.getPlugin(appKey);
        ClassLoader cl = plugin.getClassLoader();
        DispatcherEntry servletEntry = new DispatcherEntry();
        servletEntry.appKey = appKey;

        servletEntry.dispatcher = new DelegatingUBServlet(httpServlet, cl);
        servletEntry.paths = urlPatterns;
        servlets.put(servletEntry.key, servletEntry);

        for (String urlPattern : urlPatterns)
        {
            servletPathMapper.put(servletEntry.key, getLocalMountBasePath(appKey) + urlPattern);
        }
    }

    public void mountFilter(String appKey, Filter filter, String[] urlPatterns)
    {
        final Plugin plugin = pluginAccessor.getPlugin(appKey);
        ClassLoader cl = plugin.getClassLoader();
        DispatcherEntry<Filter> entry = new DispatcherEntry<Filter>();
        entry.appKey = appKey;

        entry.dispatcher = new DelegatingUBFilter(filter, cl);
        entry.paths = urlPatterns;
        filters.put(entry.key, entry);

        for (String urlPattern : urlPatterns)
        {
            filterPathMapper.put(entry.key, getLocalMountBasePath(appKey) + urlPattern);
            if (urlPattern.equals("/"))
            {
                filterPathMapper.put(entry.key, getLocalMountBasePath(appKey));
            }
        }
    }

    public void mountResources(String appKey, String resourcePrefix, String urlPattern)
    {
        final Plugin plugin = pluginAccessor.getPlugin(appKey);
        ClassLoader cl = plugin.getClassLoader();
        DispatcherEntry<HttpServlet> entry = new DispatcherEntry<HttpServlet>();
        entry.dispatcher = new DelegatingUBServlet(
                new StaticResourceServlet(plugin, resourcePrefix),
                cl);
        entry.appKey = appKey;
        servletPathMapper.put(appKey, getLocalMountBasePath(appKey) + urlPattern);
        servlets.put(appKey, entry);
    }

    @PluginEventListener
    public void unregister(PluginDisabledEvent event)
    {
        String appKey = event.getPlugin().getKey();
        for (DispatcherEntry<HttpServlet> entry : servlets.values())
        {
            if (appKey.equals(entry.appKey))
            {
                servlets.remove(entry.key);
                servletPathMapper.put(entry.key, null);
                entry.dispatcher.destroy();
            }
        }

        for (DispatcherEntry<Filter> entry : filters.values())
        {
            if (appKey.equals(entry.appKey))
            {
                filters.remove(entry.key);
                filterPathMapper.put(entry.key, null);
                entry.dispatcher.destroy();
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
        String key = servletPathMapper.get(uri);
        if (key == null)
        {
            Matcher m = APP_KEY_FINDER.matcher(uri);
            if (m.matches())
            {
                key = m.group(1);
            }
        }
        final String servletKey = key;

        List<Filter> filterList = newArrayList();
        for (String filterKey : filterPathMapper.getAll(uri))
        {
            filterList.add(getFilter(filterKey));
        }
        filterList.add(new Filter()
        {
            public void init(FilterConfig filterConfig) throws ServletException{}
            public void destroy(){}

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain
            ) throws IOException, ServletException
            {
                if (servletKey != null)
                {
                    final HttpServlet servlet = getServlet(servletKey);
                    servlet.service(request, response);
                }
                else
                {
                    chain.doFilter(request, response);
                }
            }
        });
        IteratingFilterChain localFilterChain = new IteratingFilterChain(filterList.iterator(), chain);
        localFilterChain.doFilter(request, response);


    }

    private Filter getFilter(String filterKey) throws ServletException
    {
        DispatcherEntry<Filter> entry = filters.get(filterKey);
        if (entry == null)
        {
            // stale path mapper entry
            filterPathMapper.put(filterKey, null);
            return null;
        }
        if (!entry.initialized)
        {
            FilterConfig filterConfig = new UBFilterConfig(filterKey, Collections.<String, String>emptyMap(),
                    servletContextsByApp.get(entry.appKey));
            entry.dispatcher.init(filterConfig);
            entry.initialized = true;
        }
        return entry.dispatcher;
    }

    private HttpServlet getServlet(String servletKey) throws ServletException
    {
        DispatcherEntry<HttpServlet> entry = servlets.get(servletKey);
        if (entry == null)
        {
            // stale path mapper entry
            servletPathMapper.put(servletKey, null);
            throw new IllegalStateException("Bad servlet: " + servletKey);
        }
        if (!entry.initialized)
        {
            ServletConfig servletConfig = new UBServletConfig(servletKey, Collections.<String, String>emptyMap(),
                    servletContextsByApp.get(entry.appKey));
            entry.dispatcher.init(servletConfig);
            entry.initialized = true;
        }
        return entry.dispatcher;
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

    private static class DispatcherEntry<M>
    {
        public final String key = String.valueOf(random.nextLong());
        public String appKey;
        public M dispatcher;
        public String[] paths;
        public volatile boolean initialized = false;
    }

}
