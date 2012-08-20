package com.atlassian.labs.remoteapps.plugin.module.permission;

import com.atlassian.labs.remoteapps.plugin.PermissionManager;
import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A filter to restrict incoming requests unless they have been authorized via api scopes.  Only handles 2LO-authenticated
 * requests by looking for the client key as a request attribute or a header.
 */
public class ApiScopingFilter implements Filter
{
    private final PermissionManager permissionManager;
    private final UserManager userManager;
    private static final Logger log = LoggerFactory.getLogger(ApiScopingFilter.class);
    public static final String PLUGIN_KEY = "Plugin-Key";

    public ApiScopingFilter(PermissionManager permissionManager, UserManager userManager)
    {
        this.permissionManager = permissionManager;
        this.userManager = userManager;
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
            String user = userManager.getRemoteUsername(req);
            if (!permissionManager.isRequestInApiScope(inputConsumingRequest, clientKey, user))
            {
                log.warn("Request not in an authorized API scope from app '{}' as user '{}' on URL '{}'",
                        new Object[]{clientKey, user, req.getRequestURI()});
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Request not in an authorized API scope");
                return;
            }
            log.info("Authorized app '{}' to access API at URL '{}' for user '{}'",
                    new Object[]{clientKey, req.getRequestURI(), user});
            chain.doFilter(inputConsumingRequest, response);
        }
        else
        {
            chain.doFilter(request, response);
        }

    }

    private String extractClientKey(HttpServletRequest req)
    {
        return (String) req.getAttribute(PLUGIN_KEY);
    }

    @Override
    public void destroy()
    {

    }
}
