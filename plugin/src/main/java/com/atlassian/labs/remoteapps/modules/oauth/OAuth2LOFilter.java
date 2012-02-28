package com.atlassian.labs.remoteapps.modules.oauth;

import com.atlassian.oauth.util.Check;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.AuthenticationController;
import com.atlassian.sal.api.auth.AuthenticationListener;
import com.atlassian.sal.api.auth.Authenticator;
import com.google.common.collect.ImmutableSet;
import net.oauth.OAuth;
import net.oauth.OAuthMessage;
import net.oauth.server.HttpRequestMessage;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static net.oauth.OAuth.*;

public class OAuth2LOFilter implements Filter
{
    public static final String USER_ID = "user_id";
    private static final Set<String> OAUTH_DATA_REQUEST_PARAMS = ImmutableSet.of(OAUTH_CONSUMER_KEY,
            OAUTH_SIGNATURE_METHOD,
            OAUTH_SIGNATURE,
            OAUTH_TIMESTAMP,
            OAUTH_NONCE);

    private final Authenticator authenticator;
    private final AuthenticationListener authenticationListener;
    private final AuthenticationController authenticationController;

    private final ApplicationProperties applicationProperties;

    public OAuth2LOFilter(Authenticator authenticator,
            AuthenticationListener authenticationListener,
            AuthenticationController authenticationController,
            ApplicationProperties applicationProperties)
    {
        this.authenticator = Check.notNull(authenticator, "authenticator");
        this.authenticationListener = Check.notNull(authenticationListener, "authenticationListener");
        this.authenticationController = Check.notNull(authenticationController, "authenticationController");
        this.applicationProperties = Check.notNull(applicationProperties, "applicationProperties");
    }
    
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
    {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        if (!mayProceed(request, response))
        {
            return;
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
        if (result.getPrincipal() != NonUserAdminPrincipal.INSTANCE)
        {
            authenticationListener.authenticationSuccess(result, request, response);
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
        
        boolean isRequestTokenRequest = request.getRequestURL().toString().endsWith(
                "/plugins/servlet/oauth/request-token");
        final Set<String> params = parameterNames(request);
        return  params.containsAll(OAUTH_DATA_REQUEST_PARAMS) &&
                !params.contains(OAuth.OAUTH_TOKEN) &&
                !isRequestTokenRequest;
    }

    private Set<String> parameterNames(HttpServletRequest request)
    {
        Set<String> parameterNames = new HashSet<String>();
        for (OAuth.Parameter parameter : HttpRequestMessage.getParameters(request))
        {
            parameterNames.add(parameter.getKey());
        }
        return parameterNames;
    }

    public void init(FilterConfig filterConfig) throws ServletException
    {}
    public void destroy() {}
}
