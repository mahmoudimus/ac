package com.atlassian.labs.remoteapps.modules.permissions;

import com.atlassian.labs.remoteapps.PermissionManager;
import com.google.common.collect.ImmutableSet;
import net.oauth.OAuth;
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
    private PermissionManager permissionManager;
    private static final Set<String> READ_METHODS = ImmutableSet.of(HttpMethod.GET, HttpMethod.HEAD, "OPTIONS");

    public ApiScopingFilter(PermissionManager permissionManager)
    {
        this.permissionManager = permissionManager;
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
            final String pathInfo = URI.create(req.getRequestURI().substring(req.getContextPath().length())).normalize().toString();
            final String[] elements = StringUtils.split(pathInfo, '/');
            if (elements.length > 2 && "rest".equals(elements[0]))
            {
                String api = elements[1];
                boolean writeRequest = !READ_METHODS.contains(req.getMethod());

                if (writeRequest)
                {
                    allow = permissionManager.isApiWritable(clientKey, api);
                }
                else
                {
                    allow = permissionManager.isApiReadable(clientKey, api);
                }
                if (allow)
                {
                    chain.doFilter(request, response);
                    return;
                }
                // todo: be nicer and more helpful
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            else
            {
                chain.doFilter(request, response);
            }
        }
        else
        {
            chain.doFilter(request, response);
        }

    }

    private String extractClientKey(HttpServletRequest req)
    {
        return (String) req.getAttribute(OAuth.OAUTH_CONSUMER_KEY);
    }

    @Override
    public void destroy()
    {

    }
}
