package com.atlassian.plugin.connect.plugin.module.oauth;

import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.Check;
import com.atlassian.plugin.connect.plugin.OAuthLinkManager;
import com.atlassian.plugin.connect.plugin.scopes.ApiScopingFilter;
import com.atlassian.plugin.connect.plugin.util.DefaultMessage;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.auth.AuthenticationController;
import com.atlassian.sal.api.auth.Authenticator;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import net.oauth.OAuth;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
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
     * The request attribute key that the request dispatcher uses to store the original URL for a
     * forwarded request.
     */
    private static final String FORWARD_REQUEST_URI = "javax.servlet.forward.request_uri";

    private final static Logger log = LoggerFactory.getLogger(OAuth2LOAuthenticator.class);

    private final OAuthLinkManager oAuthLinkManager;
    private final AuthenticationController authenticationController;
    private final ApplicationProperties applicationProperties;
    private final UserManager userManager;
    private final String ourConsumerKey;

    @Autowired
    public OAuth2LOAuthenticator(AuthenticationController authenticationController,
            ApplicationProperties applicationProperties,
            OAuthLinkManager oAuthLinkManager, UserManager userManager,
            ConsumerService consumerService)
    {
        this.oAuthLinkManager = oAuthLinkManager;
        this.userManager = userManager;
        this.authenticationController = Check.notNull(authenticationController,
                "authenticationController");
        this.applicationProperties = Check.notNull(applicationProperties, "applicationProperties");
        this.ourConsumerKey = consumerService.getConsumer().getKey();
    }

    public Result authenticate(HttpServletRequest request, HttpServletResponse response)
    {
        /*!
        Remotable Plugins supports a special OAuth-based authentication process that allows a Remotable Plugin
        to bypass any explicit end-user involvement to provide a more seamless user experience. The
        process is known as 2-legged OAuth combined with a way to specify the user via the request
         parameter 'user_id'.
         <p>
         To begin this process, the request is processed to extract OAuth signature information from
         the 'Authorization' request header.
         */
        OAuthMessage message = OAuthServlet.getMessage(request, getLogicalUri(request));

        /*!
        The OAuth message is validated according to against standard OAuth criteria:
        <ul>
         <li>The required fields are present</li>
         <li>The timestamp is valid</li>
         <li>The nonce matches</li>
         <li>The signature is valid</li>
        </ul>
        <p>
        Additionally, the consumer key, also known as the client key,
        matches an installed and enabled
          Remotable Plugin.
         */
        String consumerKey;
        try
        {
            consumerKey = message.getConsumerKey();
            oAuthLinkManager.validateOAuth2LORequest(message);
            if (ourConsumerKey.equals(consumerKey))
            {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null)
                {
                    consumerKey = extractPluginKey(authHeader, consumerKey);
                }
            }
        }
        /*!
        If any of these criteria fail, the authorization process is treated as a failure
          and the request will be rejected with a 403.
         */
        catch (IOException e)
        {
            log.warn("Exception authenticating request", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            return new Result.Failure(new DefaultMessage("OAuth exception:" + e.getMessage()));
        }
        catch (URISyntaxException e)
        {
            log.warn("Exception authenticating request", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            return new Result.Failure(new DefaultMessage("OAuth exception:" + e.getMessage()));
        }
        catch (OAuthProblemException ope)
        {
            logOAuthProblem(message, ope, log);
            try
            {
                OAuthServlet.handleException(response, ope, applicationProperties.getBaseUrl(UrlMode.CANONICAL));
            }
            catch (Exception e)
            {
                // there was an IOE or ServletException, nothing more we can really do
                log.error("Failure reporting OAuth error to client", e);
            }
            return new Result.Failure(new DefaultMessage(ope.getMessage()));
        }
        catch (OAuthException e)
        {
            log.warn("Exception authenticating request", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            return new Result.Failure(new DefaultMessage("OAuth exception:" + e.getMessage()));
        }

        /*!
        The user that the request is made as is specified in the request parameter 'user_id' or 'user_key'.
         */
        final String userId;
        final String userKey = request.getParameter(OAuth2LOFilter.USER_KEY);
        /*!
        The 'user_key' is checked first. If it exists, it is used to derive the 'user_id' from the application.
         */
        if (userKey != null && !"".equals(userKey))
        {
            UserProfile userProfile = userManager.getUserProfile(userKey);
            userId = userProfile == null ? null : userProfile.getUsername();
        }
        else
        {
            userId = request.getParameter(OAuth2LOFilter.USER_ID);
        }

        Principal user;
        if (userId != null && !"".equals(userId))
        {
            /*!
            The user must be a valid user in the system and must be able to both log in.
            If either of these cases fail, a 401 is returned.
             */
            user = userManager.resolve(userId);
            if (user == null || !authenticationController.canLogin(user, request))
            {
                log.warn("Access denied to user '{}' because that user cannot login", userId);
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, message);
                return new Result.Failure(new DefaultMessage("Permission denied"));
            }
        }
        /*!
        There are a few APIs provided by the application which can be accessed without specifying
        a valid user.  These are generally provided by the Remotable Plugins plugin itself.  Examples
        include:
        <ul>
         <li>Deleting an instance of a cached macro's content</li>
         <li>Deleting all cached macro content for an app</li>
        </ul>
         */
        else
        {
            user = NonUserAdminPrincipal.INSTANCE;
        }

        /*!
        If the request passed all the above checks, the app and user are marked to have
        successfully authenticated.
        <p>
        This process only authenticates the user to the host application.  From here,
        the request needs
        to be authorized to ensure it has access to the appropriate API scope.
         */
        ApiScopingFilter.setClientKey(request, consumerKey);
        log.info("Authenticated app '{}' as user '{}' successfully", consumerKey, user.getName());
        return new Result.Success(user);
        /*!-helper methods*/
    }

    static String extractPluginKey(String authHeader, String defKey)
    {
        if (authHeader.contains("realm="))
        {
            return authHeader.replaceAll(".* realm=\"([^\"]*)\".*", "$1");
        }
        else
        {
            return defKey;
        }
    }

    public static String getLogicalUri(HttpServletRequest request)
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

    public static void logOAuthProblem(final OAuthMessage message,
            final OAuthProblemException ope,
            final Logger logger)
    {
        if (OAuth.Problems.TIMESTAMP_REFUSED.equals(ope.getProblem()))
        {
            logger.warn("Rejecting OAuth request for url \"{}\" due to invalid timestamp ({}). " +
                    "This is most likely due to our system clock not being " +
                    "synchronized with the consumer's clock.",
                    new Object[] { message.URL, ope.getParameters() });
        }
        else if (logger.isDebugEnabled())
        {
            // include the full stacktrace
            logger.warn(
                    "Problem encountered authenticating OAuth client request for url \"" +
                        message.URL + "\", error was \"" + ope.getProblem() +
                            "\", with parameters \"" + ope.getParameters() + "\"", ope);
        }
        else
        {
            // omit the stacktrace
            logger.warn(
                    "Problem encountered authenticating OAuth client for url \"{}\", error was \"{}\", with parameters \"{}\"",
                    new Object[] { message.URL, ope.getProblem(), ope.getParameters() }
            );
        }
    }

    private void sendError(HttpServletResponse response, int status, OAuthMessage message)
    {
        response.setStatus(status);
        try
        {
            response.addHeader("WWW-Authenticate",
                    message.getAuthorizationHeader(applicationProperties.getBaseUrl(UrlMode.CANONICAL)));
        }
        catch (IOException e)
        {
            log.error("Failure reporting OAuth error to client", e);
        }
    }
}
