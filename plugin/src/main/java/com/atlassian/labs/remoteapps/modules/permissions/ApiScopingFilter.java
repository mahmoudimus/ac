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
            // we consume the input to allow inspection of the body via getInputStream
            InputConsumingHttpServletRequest inputConsumingRequest = new InputConsumingHttpServletRequest(req);
            if (!permissionManager.isRequestInApiScope(inputConsumingRequest, clientKey))
            {
                // todo: be nicer and more helpful
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            chain.doFilter(inputConsumingRequest, response);
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
