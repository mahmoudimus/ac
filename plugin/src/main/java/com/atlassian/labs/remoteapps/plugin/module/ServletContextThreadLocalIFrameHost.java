package com.atlassian.labs.remoteapps.plugin.module;

import com.atlassian.core.filters.ServletContextThreadLocal;
import com.atlassian.sal.api.ApplicationProperties;

import javax.servlet.http.HttpServletRequest;

import static com.google.common.base.Preconditions.*;

public final class ServletContextThreadLocalIFrameHost extends AbstractIFrameHost
{
    private final IFrameHost fallback;

    public ServletContextThreadLocalIFrameHost(ApplicationProperties applicationProperties)
    {
        this(new ApplicationPropertiesIFrameHost(applicationProperties));
    }

    public ServletContextThreadLocalIFrameHost(IFrameHost fallback)
    {
        this.fallback = checkNotNull(fallback);
    }

    @Override
    public String getContextPath()
    {
        final HttpServletRequest request = ServletContextThreadLocal.getRequest();
        if (request != null)
        {
            return request.getContextPath();
        }
        return fallback.getContextPath();
    }

    @Override
    String extractUrl()
    {
        final HttpServletRequest request = ServletContextThreadLocal.getRequest();
        if (request != null)
        {
            return request.getRequestURL().toString();
        }
        return fallback.getUrl();
    }
}
