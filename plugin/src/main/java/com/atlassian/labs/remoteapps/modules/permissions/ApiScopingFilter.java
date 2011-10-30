package com.atlassian.labs.remoteapps.modules.permissions;

import com.atlassian.labs.remoteapps.ApiPermissionManager;
import com.google.common.collect.ImmutableSet;
import electric.server.http.HTTP;
import org.apache.commons.lang.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 *
 */
public class ApiScopingFilter implements Filter
{
    private ApiPermissionManager apiPermissionManager;
    private static final Set<String> READ_METHODS = ImmutableSet.of(HttpMethod.GET, HttpMethod.HEAD, "OPTIONS");

    public ApiScopingFilter(ApiPermissionManager apiPermissionManager)
    {
        this.apiPermissionManager = apiPermissionManager;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String clientKey = extractClientKey(req);
        if (clientKey != null)
        {
            boolean allow = false;
            final String pathInfo = URI.create(req.getPathInfo()).normalize().toString();
            final String[] elements = StringUtils.split(pathInfo, '/');
            if (elements.length > 2 && "rest".equals(elements[0]))
            {
                String api = elements[1];
                boolean writeRequest = !READ_METHODS.contains(req.getMethod());

                if (writeRequest)
                {
                    allow = apiPermissionManager.isWritable(api);
                }
                else
                {
                    allow = apiPermissionManager.isReadable(api);
                }
                if (allow)
                {
                    chain.doFilter(request, response);
                    return;
                }
            }
            else
            {
                chain.doFilter(request, response);
            }

            // todo: be nicer and more helpful
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
        else
        {
            chain.doFilter(request, response);
        }

    }

    private String extractClientKey(HttpServletRequest req)
    {
        // todo
        return null;
    }

    @Override
    public void destroy()
    {

    }
}
