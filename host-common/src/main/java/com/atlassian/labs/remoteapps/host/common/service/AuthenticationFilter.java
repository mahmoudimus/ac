package com.atlassian.labs.remoteapps.host.common.service;

import com.atlassian.labs.remoteapps.api.service.RequestContext;
import com.atlassian.labs.remoteapps.api.service.SignedRequestHandler;
import com.atlassian.plugin.util.PluginUtils;
import net.oauth.OAuth;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * Captures incoming request context data and stores for later use by services in the same thread.
 */
public class AuthenticationFilter implements Filter
{
    private final SignedRequestHandler signedRequestHandler;
    private final DefaultRequestContext requestContext;
    private final boolean canSpoofAuth;

    public AuthenticationFilter(SignedRequestHandler signedRequestHandler, RequestContext requestContext)
    {
        this.signedRequestHandler = signedRequestHandler;
        this.requestContext = (DefaultRequestContext) requestContext;
        this.canSpoofAuth = Boolean.getBoolean(PluginUtils.ATLASSIAN_DEV_MODE) ||
                Boolean.getBoolean("auth.spoof");
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
            requestContext.setRequest(req);
            URI uri = URI.create(req.getRequestURI());

            AuthenticationInfo info;

            // @todo not as robust a check as it maybe could be
            if (!uri.getPath().contains("/public/") && (req.getHeader("Authorization") != null || req.getParameter(
                    OAuth.OAUTH_SIGNATURE) != null))
            {
                info = authenticateOauth(req);

                // persist to the session
                storeAuthenticationInSession(req.getSession(true), info);
            }
            else if (canSpoofAuth && req.getHeader("X-CONSUMER-KEY") != null)
            {
                info = new AuthenticationInfo();
                info.clientKey = req.getHeader("X-CONSUMER-KEY");
                info.userId = req.getHeader("X-USER-ID");

                // persist to the session
                storeAuthenticationInSession(req.getSession(true), info);
            }
            else
            {
                info = retrieveAuthenticationFromSession(req.getSession(true));
            }

            if (info != null)
            {
                requestContext.setClientKey(info.clientKey);
                requestContext.setUserId(info.userId);
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

    private AuthenticationInfo authenticateOauth(HttpServletRequest req) throws ServletException
    {
        AuthenticationInfo info = new AuthenticationInfo();
        // validate the request and obtain the client key
        info.clientKey = signedRequestHandler.validateRequest(req);

        // extract the user id, if any
        Map params = req.getParameterMap();
        String[] userIds = (String[]) params.get("user_id");
        info.userId = userIds != null && userIds.length == 1 ? userIds[0] :
            req.getHeader("RA-CTX-user-id");

        return info;
    }

    private AuthenticationInfo retrieveAuthenticationFromSession(HttpSession session)
    {
        AuthenticationInfo info = null;
        if (session.getAttribute("clientKey") != null)
        {
            info = new AuthenticationInfo();
            info.clientKey = (String) session.getAttribute("clientKey");
            info.userId = (String) session.getAttribute("userId");
        }
        return info;
    }

    private void storeAuthenticationInSession(HttpSession session, AuthenticationInfo info)
    {
        session.setAttribute("clientKey", info.clientKey);
        session.setAttribute("userId", info.userId);
    }

    @Override
    public void destroy()
    {
        requestContext.clear();
    }

    private static class AuthenticationInfo
    {
        public String clientKey;
        public String userId;
    }
}
