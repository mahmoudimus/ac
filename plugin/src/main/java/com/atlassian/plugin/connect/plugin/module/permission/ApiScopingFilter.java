package com.atlassian.plugin.connect.plugin.module.permission;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.PermissionManager;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.module.oauth.OAuth2LOAuthenticator;
import com.atlassian.plugin.connect.plugin.product.WebSudoService;
import com.atlassian.plugin.connect.plugin.xmldescriptor.XmlDescriptorExploder;
import com.atlassian.plugin.connect.spi.event.ScopedRequestAllowedEvent;
import com.atlassian.plugin.connect.spi.event.ScopedRequestDeniedEvent;
import com.atlassian.plugin.connect.spi.util.ServletUtils;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;

import org.apache.commons.lang3.StringUtils;
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

/**
 * A filter to restrict incoming requests unless they have been authorized via api scopes.  Only handles 2LO-authenticated
 * requests by looking for the client key as a request attribute or a header.
 */
public class ApiScopingFilter implements Filter
{
    /**
     * Set by a {@link Filter}, possibly using {@link OAuth2LOAuthenticator} or {@link com.atlassian.jwt.plugin.sal.JwtAuthenticator},
     * indicating the Connect add-on that is the origin of the current request.
     */
    private static final String PLUGIN_KEY = JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME;

    /**
     * Request header set by /iframe/host/main.js, indicating that the current request is an XDM request. The value
     * is the key of the Connect add-on that made the XDM request.
     */
    public static final String AP_REQUEST_HEADER = "AP-Client-Key";

    private static final Logger log = LoggerFactory.getLogger(ApiScopingFilter.class);

    private final PermissionManager permissionManager;
    private final UserManager userManager;
    private final WebSudoService webSudoService;
    private final JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    private final String ourConsumerKey;
    private final EventPublisher eventPublisher;

    public ApiScopingFilter(PermissionManager permissionManager, UserManager userManager,
            ConsumerService consumerService, WebSudoService webSudoService,
            JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService, EventPublisher eventPublisher)
    {
        this.permissionManager = permissionManager;
        this.userManager = userManager;
        this.webSudoService = webSudoService;
        this.jsonConnectAddOnIdentifierService = jsonConnectAddOnIdentifierService;
        this.eventPublisher = eventPublisher;
        this.ourConsumerKey = consumerService.getConsumer().getKey();
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

        if (isAddOnRequest(req))
        {
            // Don't accept requests when the normalised and the original request uris are not the same -- see ACDEV-656
            if (ServletUtils.normalisedAndOriginalRequestUrisDiffer(req))
            {
                log.warn("Request URI '{}' was deemed as improperly formed as it did not normalise as expected",
                        new Object[]{req.getRequestURI()});
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "The request URI is improperly formed");
                return;
            }

            // apply scopes if this is an authenticated request from
            // a/ A server-to-server call using JWT or OAuth
            // b/ A XDM bridge call from an add-on that declared scopes (== JSON descriptor)
            String addOnKey = getAddOnKeyForScopeCheck(req);
            if (addOnKey != null)
            {
                handleScopedRequest(addOnKey, req, res, chain);
                return;
            }

            // apply XDM restrictions if this is an XHR from a XML-descriptor based Connect app made via the XDM bridge
            // see AP.request() in the host AP js for more details.
            addOnKey = extractXdmRequestKey(req);
            if (addOnKey != null)
            {
                handleXdmRequest(addOnKey, req, res, chain);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private String getAddOnKeyForScopeCheck(HttpServletRequest req)
    {
        String addOnKey = extractClientKey(req);
        if (addOnKey != null)
        {
            return addOnKey;
        }
        addOnKey = extractXdmRequestKey(req);
        if (addOnKey != null && jsonConnectAddOnIdentifierService.isConnectAddOn(addOnKey))
        {
            return addOnKey;
        }
        return null;
    }

    private boolean isAddOnRequest(HttpServletRequest request)
    {
        String addOnKey = extractClientKey(request);
        return (addOnKey != null && !ourConsumerKey.equals(addOnKey)) || (extractXdmRequestKey(request) != null);
    }

    private void handleScopedRequest(String clientKey, HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException
    {
        // we consume the input to allow inspection of the body via getInputStream
        final long startTime = System.currentTimeMillis();
        InputConsumingHttpServletRequest inputConsumingRequest = new InputConsumingHttpServletRequest(req);
        UserKey user = userManager.getRemoteUserKey(req);
        HttpServletResponseWithAnalytics wrappedResponse = new HttpServletResponseWithAnalytics(res); 
        if (!permissionManager.isRequestInApiScope(inputConsumingRequest, clientKey, user))
        {
            log.warn("Request not in an authorized API scope from add-on '{}' as user '{}' on URL '{} {}'",
                    new Object[]{clientKey, user, req.getMethod(), req.getRequestURI()});
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Request not in an authorized API scope");
            eventPublisher.publish(new ScopedRequestDeniedEvent(req.getMethod(), req.getRequestURI()));
            return;
        }
        log.info("Authorized add-on '{}' to access API at URL '{} {}' for user '{}'",
                new Object[]{clientKey, req.getMethod(), req.getRequestURI(), user});

        try {
            chain.doFilter(inputConsumingRequest, wrappedResponse);
        }
        catch(Exception e)
        {
            long duration = System.currentTimeMillis() - startTime;
            eventPublisher.publish(new ScopedRequestAllowedEvent(req.getMethod(), req.getRequestURI(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, duration));
            throw ServletException.class.cast(new ServletException("Unhandled error in ApiScopingFilter").initCause(e));
        }
        long duration = System.currentTimeMillis() - startTime;
        eventPublisher.publish(new ScopedRequestAllowedEvent(req.getMethod(), req.getRequestURI(), wrappedResponse.getStatusCode(), duration));
    }

    @XmlDescriptor
    private void handleXdmRequest(String clientKey, HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException
    {
        XmlDescriptorExploder.notifyAndExplode(clientKey);

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

    /**
     * @param req the context {@link HttpServletRequest}
     * @return the unique add-on id, synonymous with OAuth client key and JWT issuer, or {@code null} if 2LO authentication failed or was not
     *         attempted
     */
    @Nullable
    public static String extractClientKey(HttpServletRequest req)
    {
        return (String) req.getAttribute(PLUGIN_KEY);
    }

    /**
     * Set the id of a Connect add-on in the request attributes.
     *
     * @param req the context {@link HttpServletRequest}
     * @return the unique add-on id, synonymous with OAuth client key and JWT issuer, or {@code null} if 2LO authentication failed or was not
     *         attempted
     */
    public static void setClientKey(@Nonnull HttpServletRequest req, @Nonnull String clientKey)
    {
        req.setAttribute(PLUGIN_KEY, clientKey);
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
}
