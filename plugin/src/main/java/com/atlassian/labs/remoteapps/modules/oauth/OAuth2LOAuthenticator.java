package com.atlassian.labs.remoteapps.modules.oauth;

import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.util.DefaultMessage;
import com.atlassian.oauth.util.Check;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.AuthenticationController;
import com.atlassian.sal.api.auth.Authenticator;
import com.atlassian.sal.api.user.UserManager;
import net.oauth.OAuth;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.server.OAuthServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;

/**
 * Authenticates an incoming 2LO request
 */
@Component
public class OAuth2LOAuthenticator implements Authenticator
{
    /**
     * The request attribute key that the request dispatcher uses to store the
     * original URL for a forwarded request.
     */
    private static final String FORWARD_REQUEST_URI = "javax.servlet.forward.request_uri";
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final OAuthLinkManager oAuthLinkManager;
    private final AuthenticationController authenticationController;
    private final ApplicationProperties applicationProperties;
    private final UserManager userManager;
    private final PermissionManager permissionManager;

    @Autowired
    public OAuth2LOAuthenticator(AuthenticationController authenticationController,
                                 ApplicationProperties applicationProperties,
                                 OAuthLinkManager oAuthLinkManager, UserManager userManager, PermissionManager permissionManager)
    {
        this.oAuthLinkManager = oAuthLinkManager;
        this.userManager = userManager;
        this.permissionManager = permissionManager;
        this.authenticationController = Check.notNull(authenticationController, "authenticationController");
        this.applicationProperties = Check.notNull(applicationProperties, "applicationProperties");
    }
    
    public Result authenticate(HttpServletRequest request, HttpServletResponse response)
    {
        OAuthMessage message = OAuthServlet.getMessage(request, getLogicalUri(request));

        String consumerKey;
        try
        {
            consumerKey = message.getConsumerKey();
            oAuthLinkManager.validateOAuth2LORequest(message);
        }
        catch (IOException e)
        {
            log.warn("Exception authenticating request", e);
            return new Result.Failure(new DefaultMessage("OAuth exception:" + e.getMessage()));
        }
        catch (URISyntaxException e)
        {
            log.warn("Exception authenticating request", e);
            return new Result.Failure(new DefaultMessage("OAuth exception:" + e.getMessage()));
        }
        catch (OAuthException e)
        {
            log.warn("Exception authenticating request", e);
            return new Result.Failure(new DefaultMessage("OAuth exception:" + e.getMessage()));
        }

        final String userId = request.getParameter(OAuth2LOFilter.USER_ID);
        Check.notNull(userId);
        final Principal user = userManager.resolve(userId);
        if (!authenticationController.canLogin(user, request))
        {
            // user exists but is not allowed to login
            log.warn("Access denied to user '{}' because that user cannot login", userId);
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, message);
            return new Result.Failure(new DefaultMessage("Permission denied"));
        }
        else if (!permissionManager.canAccessApi(userId, consumerKey))
        {
            // user exists but is not allowed to access this remote app
            log.warn("Access denied to user '{}' because that user is not allowed to make api calls from the remote app '{}'", userId, consumerKey);
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, message);
            return new Result.Failure(new DefaultMessage("Permission denied"));
        }
        request.setAttribute(OAuth.OAUTH_CONSUMER_KEY, consumerKey);
        return new Result.Success(user);
    }

    private String getLogicalUri(HttpServletRequest request)
    {
        String uriPathBeforeForwarding = (String) request.getAttribute(FORWARD_REQUEST_URI);
        if (uriPathBeforeForwarding == null)
        {
            return null;
        }
        URI newUri = URI.create(request.getRequestURL().toString());
        try
        {
            return new URI(newUri.getScheme(), newUri.getAuthority(),
                    uriPathBeforeForwarding,
                    newUri.getQuery(),
                    newUri.getFragment()).toString();
        }
        catch (URISyntaxException e)
        {
            log.warn("forwarded request had invalid original URI path: " + uriPathBeforeForwarding);
            return null;
        }
    }

    private void sendError(HttpServletResponse response, int status, OAuthMessage message)
    {
        response.setStatus(status);
        try
        {
            response.addHeader("WWW-Authenticate", message.getAuthorizationHeader(applicationProperties.getBaseUrl()));
        }
        catch (IOException e)
        {
            log.error("Failure reporting OAuth error to client", e);
        }
    }
}
