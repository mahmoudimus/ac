package com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jwt.core.Clock;
import com.atlassian.jwt.core.SystemClock;
import com.atlassian.plugin.connect.api.auth.scope.AddonKeyExtractor;
import com.atlassian.plugin.connect.api.util.ServletUtils;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(ApiScopingFilter.class);

    private final AddonScopeManager addonScopeManager;
    private final UserManager userManager;
    private final EventPublisher eventPublisher;
    private final Clock clock;
    private final AddonKeyExtractor addonKeyExtractor;

    public ApiScopingFilter(AddonScopeManager addonScopeManager, UserManager userManager,
            EventPublisher eventPublisher, AddonKeyExtractor addonKeyExtractor)
    {
        this(addonScopeManager,
             userManager,
             eventPublisher,
                addonKeyExtractor,
             new SystemClock());
    }

    public ApiScopingFilter(AddonScopeManager addonScopeManager, UserManager userManager,
            EventPublisher eventPublisher, AddonKeyExtractor addonKeyExtractor, Clock clock)
    {
        this.addonScopeManager = addonScopeManager;
        this.userManager = userManager;
        this.eventPublisher = eventPublisher;
        this.addonKeyExtractor = addonKeyExtractor;
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

        if (addonKeyExtractor.isAddonRequest(req))
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
            String addonKey = addonKeyExtractor.getAddonKeyFromHttpRequest(req);
            if (addonKey != null)
            {
                handleScopedRequest(addonKey, req, res, chain);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private void handleScopedRequest(String addonKey, HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException
    {
        final long startTime = clock.now().getTime();
        // we consume the input to allow inspection of the body via getInputStream
        InputConsumingHttpServletRequest inputConsumingRequest = new InputConsumingHttpServletRequest(req);
        UserKey user = userManager.getRemoteUserKey(req);
        HttpServletResponseWithAnalytics wrappedResponse = new HttpServletResponseWithAnalytics(res);
        if (!addonScopeManager.isRequestInApiScope(inputConsumingRequest, addonKey))
        {
            log.warn("Request not in an authorized API scope from add-on '{}' as user '{}' on URL '{} {}'",
                    new Object[]{addonKey, user, req.getMethod(), req.getRequestURI()});
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Request not in an authorized API scope");
            eventPublisher.publish(new ScopedRequestDeniedEvent(req, addonKey));
            return;
        }
        log.debug("Authorized add-on '{}' to access API at URL '{} {}' for user '{}'",
                new Object[]{addonKey, req.getMethod(), req.getRequestURI(), user});

        try {
            chain.doFilter(inputConsumingRequest, wrappedResponse);
        }
        catch(Exception e)
        {
            long duration = clock.now().getTime() - startTime;
            eventPublisher.publish(new ScopedRequestAllowedEvent(req, addonKey, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, duration));
            throw ServletException.class.cast(new ServletException("Unhandled error in ApiScopingFilter").initCause(e));
        }
        long duration = clock.now().getTime() - startTime;
        eventPublisher.publish(new ScopedRequestAllowedEvent(req, addonKey, wrappedResponse.getStatusCode(), duration));
    }

    @Override
    public void destroy()
    {

    }
}
