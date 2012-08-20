package com.atlassian.labs.remoteapps.plugin.loader;

import com.atlassian.labs.remoteapps.api.service.HttpResourceMounter;
import com.atlassian.labs.remoteapps.api.service.RequestContext;
import com.atlassian.labs.remoteapps.api.service.SignedRequestHandler;
import com.atlassian.labs.remoteapps.host.common.service.AuthenticationFilter;
import com.atlassian.labs.remoteapps.plugin.loader.universalbinary.UBDispatchFilter;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
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
    private static final Logger log = LoggerFactory.getLogger(LocalHttpResourceMounter.class);
    private final String appKey;

    public LocalHttpResourceMounter(Bundle bundle,
                                    UBDispatchFilter httpResourceFilter,
                                    SignedRequestHandler signedRequestHandler,
                                    RequestContext requestContext
    )
    {
        this.httpResourceFilter = httpResourceFilter;
        this.appKey = OsgiHeaderUtil.getPluginKey(bundle);
        mountFilter(new AuthenticationFilter(signedRequestHandler, requestContext), "/*");
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
