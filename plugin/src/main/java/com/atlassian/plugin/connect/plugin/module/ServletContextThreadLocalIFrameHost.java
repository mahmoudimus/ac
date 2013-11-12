package com.atlassian.plugin.connect.plugin.module;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.core.filters.ServletContextThreadLocal;
import com.atlassian.sal.api.ApplicationProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public final class ServletContextThreadLocalIFrameHost extends AbstractIFrameHost
{
    private final IFrameHost fallback;

    @Autowired
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
    public URI getUrl()
    {
        final HttpServletRequest request = ServletContextThreadLocal.getRequest();
        if (request != null)
        {
            return createEasyXdmHost(URI.create(request.getRequestURL().toString()));
        }
        return fallback.getUrl();
    }
}
