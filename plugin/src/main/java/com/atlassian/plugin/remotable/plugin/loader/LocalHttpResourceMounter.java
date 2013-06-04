package com.atlassian.plugin.remotable.plugin.loader;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.remotable.api.service.HttpResourceMounter;
import com.atlassian.plugin.remotable.api.service.RequestContext;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.api.service.http.bigpipe.BigPipeManager;
import com.atlassian.plugin.remotable.host.common.service.AuthenticationFilter;
import com.atlassian.plugin.remotable.host.common.service.http.bigpipe.BigPipeContentFilter;
import com.atlassian.plugin.remotable.host.common.service.http.bigpipe.BigPipeRequestIdFilter;
import com.atlassian.plugin.remotable.host.common.service.http.bigpipe.DefaultBigPipeManager;
import com.atlassian.plugin.remotable.plugin.loader.universalbinary.UBDispatchFilter;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import com.atlassian.plugin.remotable.plugin.loader.universalbinary.UBDocumentationRedirectFilter;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;

/**
 * Allows local http resources to be mounted within the product
 */
public class LocalHttpResourceMounter implements HttpResourceMounter
{
    private final UBDispatchFilter httpResourceFilter;
    private final String appKey;

    public LocalHttpResourceMounter(
            Plugin plugin, UBDispatchFilter httpResourceFilter,
            SignedRequestHandler signedRequestHandler,
            RequestContext requestContext,
            DefaultBigPipeManager bigPipeManager)
    {
        this.httpResourceFilter = httpResourceFilter;
        this.appKey = plugin.getKey();
        mountFilter(new BigPipeRequestIdFilter(bigPipeManager), "/*");
        mountFilter(new UBDocumentationRedirectFilter(plugin), "/");

        mountFilter(new AuthenticationFilter(signedRequestHandler, requestContext), "/*");
        mountFilter(new BigPipeContentFilter(bigPipeManager), "/bigpipe/request/*");
    }

    @Override
    public String getLocalMountBaseUrl()
    {
        return httpResourceFilter.getLocalMountBaseUrl(appKey);
    }

    @Override
    public void mountFilter(Filter filter, String... urlPatterns)
    {
        httpResourceFilter.mountFilter(appKey, filter, urlPatterns);
    }

    @Override
    public void mountServlet(HttpServlet httpServlet, String... urlPatterns)
    {
        httpResourceFilter.mountServlet(appKey, httpServlet, urlPatterns);
    }

    @Override
    public void mountStaticResources(String resourcePrefix, String urlPattern)
    {
        httpResourceFilter.mountResources(appKey, resourcePrefix, urlPattern);
    }
}
