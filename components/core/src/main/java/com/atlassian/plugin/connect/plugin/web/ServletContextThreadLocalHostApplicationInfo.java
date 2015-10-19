package com.atlassian.plugin.connect.plugin.web;

import com.atlassian.core.filters.ServletContextThreadLocal;
import com.atlassian.sal.api.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public final class ServletContextThreadLocalHostApplicationInfo extends AbstractHostApplicationInfo
{
    private final HostApplicationInfo fallback;

    @Autowired
    public ServletContextThreadLocalHostApplicationInfo(ApplicationProperties applicationProperties)
    {
        this(new ApplicationPropertiesHostApplicationInfo(applicationProperties));
    }

    public ServletContextThreadLocalHostApplicationInfo(HostApplicationInfo fallback)
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
