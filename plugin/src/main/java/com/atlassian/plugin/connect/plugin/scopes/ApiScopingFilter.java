package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.core.Clock;
import com.atlassian.jwt.core.SystemClock;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.module.oauth.OAuth2LOAuthenticator;
import com.atlassian.plugin.connect.plugin.module.permission.HttpServletResponseWithAnalytics;
import com.atlassian.plugin.connect.plugin.module.permission.InputConsumingHttpServletRequest;
import com.atlassian.plugin.connect.spi.event.ScopedRequestAllowedEvent;
import com.atlassian.plugin.connect.spi.event.ScopedRequestDeniedEvent;
import com.atlassian.plugin.connect.spi.util.ServletUtils;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

    private final AddOnScopeManager addOnScopeManager;
    private final UserManager userManager;
    private final JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    private final String ourConsumerKey;
    private final EventPublisher eventPublisher;
    private final Clock clock;

    public ApiScopingFilter(AddOnScopeManager addOnScopeManager, UserManager userManager,
        ConsumerService consumerService, JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService, EventPublisher eventPublisher)
    {
        this(addOnScopeManager,
             userManager,
             consumerService,
             jsonConnectAddOnIdentifierService,
             eventPublisher,
             new SystemClock());
    }

    public ApiScopingFilter(AddOnScopeManager addOnScopeManager, UserManager userManager,
            ConsumerService consumerService, JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService, EventPublisher eventPublisher, Clock clock)
    {
        this.addOnScopeManager = addOnScopeManager;
        this.userManager = userManager;
        this.jsonConnectAddOnIdentifierService = jsonConnectAddOnIdentifierService;
        this.eventPublisher = eventPublisher;
        this.ourConsumerKey = consumerService.getConsumer().getKey();
        this.clock = clock;
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
        final long startTime = clock.now().getTime();
        // we consume the input to allow inspection of the body via getInputStream
        InputConsumingHttpServletRequest inputConsumingRequest = new InputConsumingHttpServletRequest(req);
        UserKey user = userManager.getRemoteUserKey(req);
        HttpServletResponseWithAnalytics wrappedResponse = new HttpServletResponseWithAnalytics(res);
        if (!addOnScopeManager.isRequestInApiScope(inputConsumingRequest, clientKey, user))
        {
            log.warn("Request not in an authorized API scope from add-on '{}' as user '{}' on URL '{} {}'",
                    new Object[]{clientKey, user, req.getMethod(), req.getRequestURI()});
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Request not in an authorized API scope");
            eventPublisher.publish(new ScopedRequestDeniedEvent(req));
            return;
        }
        log.info("Authorized add-on '{}' to access API at URL '{} {}' for user '{}'",
                new Object[]{clientKey, req.getMethod(), req.getRequestURI(), user});

        try {
            chain.doFilter(inputConsumingRequest, wrappedResponse);
        }
        catch(Exception e)
        {
            long duration = clock.now().getTime() - startTime;
            eventPublisher.publish(new ScopedRequestAllowedEvent(req, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, duration));
            throw ServletException.class.cast(new ServletException("Unhandled error in ApiScopingFilter").initCause(e));
        }
        long duration = clock.now().getTime() - startTime;
        eventPublisher.publish(new ScopedRequestAllowedEvent(req, wrappedResponse.getStatusCode(), duration));
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
