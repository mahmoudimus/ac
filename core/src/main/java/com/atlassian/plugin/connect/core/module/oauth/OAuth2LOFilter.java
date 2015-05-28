package com.atlassian.plugin.connect.core.module.oauth;

import com.atlassian.oauth.util.Check;
import com.atlassian.plugin.connect.spi.product.WebSudoService;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.auth.AuthenticationController;
import com.atlassian.sal.api.auth.AuthenticationListener;
import com.atlassian.sal.api.auth.Authenticator;
import com.google.common.collect.ImmutableSet;
import net.oauth.OAuth;
import net.oauth.server.HttpRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

import static net.oauth.OAuth.*;

public class OAuth2LOFilter implements Filter
{
    public static final String USER_ID = "user_id";
    public static final String USER_KEY = "user_key";
    private static final Set<String> OAUTH_DATA_REQUEST_PARAMS = ImmutableSet.of(OAuth.OAUTH_CONSUMER_KEY,
            OAuth.OAUTH_SIGNATURE_METHOD,
            OAuth.OAUTH_SIGNATURE,
            OAuth.OAUTH_TIMESTAMP,
            OAuth.OAUTH_NONCE);
    private final static Logger log = LoggerFactory.getLogger(OAuth2LOFilter.class);

    private final Authenticator authenticator;
    private final AuthenticationListener authenticationListener;
    private final AuthenticationController authenticationController;
    private final WebSudoService webSudoService;
    private final ApplicationProperties applicationProperties;

    public OAuth2LOFilter(Authenticator authenticator,
                          AuthenticationListener authenticationListener,
                          AuthenticationController authenticationController,
                          WebSudoService webSudoService,
                          ApplicationProperties applicationProperties)
    {
        this.webSudoService = Check.notNull(webSudoService, "webSudoService");
        this.authenticator = Check.notNull(authenticator, "authenticator");
        this.authenticationListener = Check.notNull(authenticationListener, "authenticationListener");
        this.authenticationController = Check.notNull(authenticationController, "authenticationController");
        this.applicationProperties = Check.notNull(applicationProperties, "applicationProperties");
    }
    
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
    {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        try
        {
            if (!mayProceed(request, response))
            {
                return;
            }
        }
        catch (RuntimeException ex)
        {
            // ignore this exception as it only happens if the plugin has been shutdown while
            // the request has been in this filter
            if (!ex.getClass().getSimpleName().equals("ServiceProxyDestroyedException"))
            {
                throw ex;
            }
            else
            {
                log.debug(ex.toString());
            }
        }
        try
        {
            chain.doFilter(request, response);
        }
        finally
        {
            if (isOAuth2LOAccessAttempt(request) && request.getSession(false) != null)
            {
                request.getSession().invalidate();
            }
        }

    }

    boolean mayProceed(HttpServletRequest request, HttpServletResponse response)
    {
        // is it a protected resource? if not, we don't care
        if (!authenticationController.shouldAttemptAuthentication(request))
        {
            authenticationListener.authenticationNotAttempted(request, response);
            return true;
        }

        // are the oauth parameters present?
        if (!isOAuth2LOAccessAttempt(request))
        {
            // if the oauth parameters aren't present, we allow the filter chain to continue being processed,
            // but we want to add the WWW-Authenticate header
            authenticationListener.authenticationNotAttempted(request, response);
            return true;
        }
            
        final Authenticator.Result result = authenticator.authenticate(request, response);
        if (result.getStatus() == Authenticator.Result.Status.FAILED)
        {
            authenticationListener.authenticationFailure(result, request, response);
            return false;
        }
        
        if (result.getStatus() == Authenticator.Result.Status.ERROR)
        {
            authenticationListener.authenticationError(result, request, response);
            return false;
        }

        // can only mark the request as successfully authenticated if the user is a real one
        if (result.getStatus() == Authenticator.Result.Status.SUCCESS)
        {
            authenticationListener.authenticationSuccess(result, request, response);
            webSudoService.startWebSudoSession(request, response);
        }

        //markAsOAuthRequest(request);

        return true;
    }

    /**
     * We're trying to access an OAuth protected resource if all the OAuth parameters are set and we aren't trying to
     * turn a request token into an access token (which is the only other time all the OAuth parameters are in the
     * request).
     */
    private boolean isOAuth2LOAccessAttempt(HttpServletRequest request)
    {
        final Set<String> names = parameterNames(request);
        return  names.containsAll(OAUTH_DATA_REQUEST_PARAMS) &&
                !names.contains(OAuth.OAUTH_TOKEN) &&
                !isTokenRequest(request) &&
                !isDownloadableResourceRequest(request);
    }

    private boolean isDownloadableResourceRequest(HttpServletRequest request)
    {
        return request.getRequestURI().startsWith(getContextPath(request) + "/download/resources/");
    }

    private boolean isTokenRequest(HttpServletRequest request)
    {
        return request.getRequestURL().toString().endsWith("/plugins/servlet/oauth/request-token");
    }

    private String getContextPath(HttpServletRequest request)
    {
        final String baseUrl = applicationProperties.getBaseUrl(UrlMode.CANONICAL);
        if (baseUrl == null)
        {
            return request.getContextPath();
        }
        return URI.create(baseUrl).getPath();
    }

    private Set<String> parameterNames(HttpServletRequest request)
    {
        final ImmutableSet.Builder<String> names = ImmutableSet.builder();
        for (OAuth.Parameter parameter : HttpRequestMessage.getParameters(request))
        {
            names.add(parameter.getKey());
        }
        return names.build();
    }

    public void init(FilterConfig filterConfig) throws ServletException
    {

    }

    public void destroy()
    {

    }
}
