package com.atlassian.plugin.connect.plugin.module.permission;

import com.atlassian.plugin.connect.plugin.PermissionManager;
import com.atlassian.plugin.connect.plugin.product.WebSudoService;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static com.atlassian.plugin.connect.plugin.util.DevModeUtil.DEV_MODE_ENABLED;

public abstract class ApiScopingFilterBase implements Filter, RequestAddOnKeyLabeler
{
    private final PermissionManager permissionManager;
    private final UserManager userManager;
    private final WebSudoService webSudoService;
    private final String ourConsumerKey;

    /**
     * Set by an authorisation {@link Filter} calling {@link #setAddOnKey(HttpServletRequest, String)} to indicate the Connect add-on that is the origin of the current request.
     */
    private static final String ADD_ON_KEY = "Add-On-Key";

    /**
     * Request header set by /iframe/host/main.js, indicating that the current request is an XDM request. The value
     * is the key of the Connect add-on that made the XDM request.
     */
    private static final String AP_REQUEST_HEADER = "AP-Client-Key";

    private static final Logger log = LoggerFactory.getLogger(ApiScopingFilterBase.class);

    protected ApiScopingFilterBase(PermissionManager permissionManager, UserManager userManager, WebSudoService webSudoService, String ourConsumerKey)
    {
        this.permissionManager = permissionManager;
        this.userManager = userManager;
        this.webSudoService = webSudoService;
        this.ourConsumerKey = ourConsumerKey;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // apply scopes if this is an authenticated request from a Connect app
        String clientKey = getAddOnKey(req);
        if (clientKey != null && !ourConsumerKey.equals(clientKey))
        {
            handleScopedRequest(clientKey, req, res, chain);
            return;
        }

        // apply XDM restrictions if this is an XHR from a Connect app made via the XDM bridge
        // see AP.request() in the host AP js for more details.
        clientKey = extractXdmRequestKey(req);
        if (clientKey != null)
        {
            handleXdmRequest(clientKey, req, res, chain);
            return;
        }

        chain.doFilter(request, response);
    }

    private void handleScopedRequest(String clientKey, HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException
    {
        // we consume the input to allow inspection of the body via getInputStream
        InputConsumingHttpServletRequest inputConsumingRequest = new InputConsumingHttpServletRequest(req);
        UserKey userKey = userManager.getRemoteUserKey(req);
        if (!permissionManager.isRequestInApiScope(inputConsumingRequest, clientKey, userKey))
        {
            log.warn("Request not in an authorized API scope from app '{}' as userKey '{}' on URL '{}'",
                    new Object[]{clientKey, userKey, req.getRequestURI()});
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Request not in an authorized API scope");
            return;
        }
        log.info("Authorized app '{}' to access API at URL '{}' for userKey '{}'",
                new Object[]{clientKey, req.getRequestURI(), userKey});
        chain.doFilter(inputConsumingRequest, res);
    }

    private void handleXdmRequest(String clientKey, HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException
    {
        // prevent all XDM requests made on behalf of system administrators
        UserKey userKey = userManager.getRemoteUserKey(req);
        if (!DEV_MODE_ENABLED && userKey != null && userManager.isSystemAdmin(userKey))
        {
            log.warn("XDM request from app '{}' attempted to authenticate as a system administrator '{}' when " +
                    "accessing the resource '{}', and was denied.",
                    new Object[]{clientKey, userKey, req.getRequestURI()});
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "AP.request() can not be used to access WebSudo " +
                    "protected APIs.");
            return;
        }

        // suspend the current WebSudo session if present to prevent access to all WebSudo protected APIs
        HttpSession session = req.getSession(false);
        if (session != null)
        {
            // there's no API for suspending a WebSudo session, so unfortunately we have to manually clear
            // and then re-instate the session parameter (a timestamp in JIRA and Confluence) used to
            // mark a session as WebSudo'd. This is bit of an unfortunate hack, but can be removed once
            // we've sorted out Connect app the permissions vs. scopes debacle. (ACDEV-369)
            String webSudoSessionKey = webSudoService.getWebSudoSessionKey();
            Object webSudoTimestamp = session.getAttribute(webSudoSessionKey);
            if (webSudoTimestamp != null)
            {
                session.setAttribute(webSudoSessionKey, null);
                try
                {
                    chain.doFilter(req, res);
                }
                finally
                {
                    // restore the current WebSudo session (if a new one hasn't been established)
                    Object attribute = session.getAttribute(webSudoSessionKey);
                    if (attribute == null)
                    {
                        session.setAttribute(webSudoSessionKey, webSudoTimestamp);
                    }
                    else
                    {
                        // This is not expected behaviour, but should be harmless as a subsequent request will still
                        // have it's WebSudo session suspended.
                        log.warn("XDM request unexpectedly initiated a new WebSudo session expiring at " + attribute);
                    }
                }
                return;
            }
        }

        // no WebSudo session - carry on
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    /**
     * @param req the context {@link javax.servlet.http.HttpServletRequest}
     * @return a {@link #AP_REQUEST_HEADER header} set by the XDM host library, indicating the current request is a host-mediated XHR sent on
     *         behalf of an add-on running in a sandboxed iframe; see AP.request(...) in the host-side AP js
     */
    @Nullable
    public static String extractXdmRequestKey(HttpServletRequest req)
    {
        return req.getHeader(AP_REQUEST_HEADER);
    }

    @Override
    public void destroy()
    {
    }

    @Override
    public String getAddOnKey(@Nonnull HttpServletRequest request)
    {
        return (String) request.getAttribute(ADD_ON_KEY);
    }

    @Override
    public void setAddOnKey(@Nonnull HttpServletRequest request, @Nonnull String addOnKey)
    {
        request.setAttribute(ADD_ON_KEY, addOnKey);
    }
}
