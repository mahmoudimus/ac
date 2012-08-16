package com.atlassian.labs.remoteapps.api.services.impl;

import com.atlassian.labs.remoteapps.api.services.RequestContext;
import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import net.oauth.OAuth;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * Captures incoming request context data and stores for later use by services in the same thread.
 */
public class AuthenticationFilter implements Filter
{
    private SignedRequestHandler signedRequestHandler;
    private DefaultRequestContext requestContext;

    public AuthenticationFilter(SignedRequestHandler signedRequestHandler, RequestContext requestContext)
    {
        this.signedRequestHandler = signedRequestHandler;
        this.requestContext = (DefaultRequestContext) requestContext;
    }

    @Override
    public void init(FilterConfig filterConfig)
        throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        try
        {
            HttpServletRequest req = (HttpServletRequest) request;
            URI uri = URI.create(req.getRequestURI());

            // @todo not as robust a check as it maybe could be
            if (!uri.getPath().contains("/public/") && (req.getHeader("Authorization") != null || req.getParameter(
                    OAuth.OAUTH_SIGNATURE) != null))
            {
                // validate the request and obtain the client key
                String clientKey = signedRequestHandler.validateRequest(req);
                requestContext.setClientKey(clientKey);

                // extract the user id, if any
                Map params = req.getParameterMap();
                String[] userIds = (String[]) params.get("user_id");
                String userId = userIds != null && userIds.length == 1 ? userIds[0] : null;
                requestContext.setUserId(userId);
            }

            // complete the request handling
            chain.doFilter(request, response);
        }
        finally
        {
            // clear the auth data state
            requestContext.clear();
        }
    }

    @Override
    public void destroy()
    {
        requestContext.clear();
    }
}
