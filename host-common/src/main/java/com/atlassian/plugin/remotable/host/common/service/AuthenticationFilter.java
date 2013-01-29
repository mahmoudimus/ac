package com.atlassian.plugin.remotable.host.common.service;

import com.atlassian.plugin.remotable.api.service.RequestContext;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.host.common.service.http.DefaultRequestContext;
import com.atlassian.plugin.util.PluginUtils;
import net.oauth.OAuth;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * Captures incoming sreq context data and stores for later use by services in the same thread.
 */
public class AuthenticationFilter implements Filter
{
    private static final String AUTH_COOKIE_NAME = "AP-Auth-State";

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
    public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain)
        throws IOException, ServletException
    {
        try
        {
            HttpServletRequest req = (HttpServletRequest) sreq;
            HttpServletResponse res = (HttpServletResponse) sres;
            requestContext.setRequest(req);
            URI uri = URI.create(req.getRequestURI());

            AuthenticationInfo info;

            // @todo not as robust a check as it maybe could be
            if (!uri.getPath().contains("/public/") && (req.getHeader("Authorization") != null || req.getParameter(
                    OAuth.OAUTH_SIGNATURE) != null))
            {
                info = authenticateOauth(req);

                // persist to the session
                storeAuthentication(res, info);
            }
            else if (canSpoofAuth && req.getHeader("X-CONSUMER-KEY") != null)
            {
                String clientKey = req.getHeader("X-CONSUMER-KEY");
                String userId = req.getHeader("X-USER-ID");
                info = new AuthenticationInfo(clientKey, userId);

                // persist to the session
                storeAuthentication(res, info);
            }
            else
            {
                info = retrieveAuthentication(req);
            }

            if (info != null)
            {
                requestContext.setClientKey(info.getClientKey());
                requestContext.setUserId(info.getUserId());
            }

            // complete the request handling
            chain.doFilter(sreq, sres);
        }
        finally
        {
            // clear the auth data state
            DefaultRequestContext.clear();
        }
    }

    private AuthenticationInfo authenticateOauth(HttpServletRequest req) throws ServletException
    {
        // validate the request and obtain the client key
        String clientKey = signedRequestHandler.validateRequest(req);

        // extract the user id, if any
        Map params = req.getParameterMap();
        String[] userIds = (String[]) params.get("user_id");
        String userId = userIds != null && userIds.length == 1 ? userIds[0] : req.getHeader("AP-CTX-user-id");

        return new AuthenticationInfo(clientKey, userId);
    }

    private AuthenticationInfo retrieveAuthentication(HttpServletRequest req)
    {
        AuthenticationInfo info = null;
        Cookie[] cookies = req.getCookies();
        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                String name = cookie.getName();
                if (name != null && name.equals(AUTH_COOKIE_NAME))
                {
                    info = AuthenticationInfo.decode(cookie.getValue());
                    break;
                }
            }
            if (info == null)
            {
                String header = req.getHeader(AUTH_COOKIE_NAME);
                if (header != null)
                {
                    info = AuthenticationInfo.decode(header);
                }
            }
        }
        return info;
    }

    private void storeAuthentication(HttpServletResponse res, AuthenticationInfo info)
    {
        res.addCookie(new Cookie(AUTH_COOKIE_NAME, info.encode()));
    }

    @Override
    public void destroy()
    {
        DefaultRequestContext.clear();
    }
}
