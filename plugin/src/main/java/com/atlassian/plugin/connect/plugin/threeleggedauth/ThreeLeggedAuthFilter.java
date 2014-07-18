package com.atlassian.plugin.connect.plugin.threeleggedauth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.jwt.core.http.auth.SimplePrincipal;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.plugin.service.LegacyAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.util.DefaultMessage;
import com.atlassian.sal.api.auth.AuthenticationListener;
import com.atlassian.sal.api.auth.Authenticator;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;

import static com.atlassian.jwt.JwtConstants.AppLinks.SYS_PROP_ALLOW_IMPERSONATION;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Boolean.getBoolean;

@Component
public class ThreeLeggedAuthFilter implements Filter
{
    private final ThreeLeggedAuthService threeLeggedAuthService;
    private final ConnectAddonManager connectAddonManager;
    private final UserManager userManager;
    private final AuthenticationListener authenticationListener;
    private final JwtApplinkFinder jwtApplinkFinder;
    private final CrowdService crowdService;
    private final String badCredentialsMessage; // protect against phishing by not saying whether the add-on, user or secret was wrong
    private final LegacyAddOnIdentifierService legacyAddOnIdentifierService;

    private final static Logger log = LoggerFactory.getLogger(ThreeLeggedAuthFilter.class);
    private static final String MSG_FORMAT_NOT_ALLOWING_IMPERSONATION = "NOT allowing add-on '%s' to impersonate user '%s'";

    @Autowired
    public ThreeLeggedAuthFilter(ThreeLeggedAuthService threeLeggedAuthService,
                                 ConnectAddonManager connectAddonManager,
                                 UserManager userManager,
                                 AuthenticationListener authenticationListener,
                                 JwtApplinkFinder jwtApplinkFinder,
                                 CrowdService crowdService,
                                 I18nResolver i18nResolver,
                                 LegacyAddOnIdentifierService legacyAddOnIdentifierService)
    {
        this.threeLeggedAuthService = checkNotNull(threeLeggedAuthService);
        this.connectAddonManager = checkNotNull(connectAddonManager);
        this.userManager = checkNotNull(userManager);
        this.authenticationListener = checkNotNull(authenticationListener);
        this.jwtApplinkFinder = checkNotNull(jwtApplinkFinder);
        this.crowdService = checkNotNull(crowdService);
        this.badCredentialsMessage = i18nResolver.getText("connect.3la.bad_credentials");
        this.legacyAddOnIdentifierService = legacyAddOnIdentifierService;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    private static class InvalidSubjectException extends Exception
    {
        public InvalidSubjectException(String username)
        {
            super(username);
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        Object addOnKeyObject = servletRequest.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME);
        String addOnKey = addOnKeyObject instanceof String ? (String)addOnKeyObject : null;

        // warn if weird properties are set
        if (null != addOnKeyObject && !(addOnKeyObject instanceof String))
        {
            log.warn("The value of the request attribute '{}' should be a string but instead it is a '{}': '{}'. This is a programming error in the code that sets this value.",
                    new Object[]{JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME, addOnKeyObject.getClass().getSimpleName(), addOnKeyObject});
        }

        // potentially reject only if the request comes from an add-on
        if (StringUtils.isEmpty(addOnKey) || legacyAddOnIdentifierService.isConnectAddOn(addOnKey))
        {
            filterChain.doFilter(request, response);
        }
        else
        {
            processAddOnRequest(filterChain, request, response, addOnKey);
        }
    }

    private void processAddOnRequest(FilterChain filterChain, HttpServletRequest request, HttpServletResponse response, String addOnKey) throws IOException, ServletException
    {
        Object subjectObject = request.getAttribute(JwtConstants.HttpRequests.JWT_SUBJECT_ATTRIBUTE_NAME);
        String subject = subjectObject instanceof String ? (String) subjectObject : null;

        // an empty-string subject claim would be nonsensical and may indicate a programming error in the add-on
        if ("".equals(subject))
        {
            rejectEmptyStringSubject(request, response, addOnKey, subject);
        }
        else
        {
            // ACDEV-1304: Ensure that any add-on requests have no effect on a session that may already exist.
            HttpSession session = request.getSession(false); // don't create a session if there is none
            try
            {
                if (null != subject && shouldAllowImpersonation(request, response, addOnKey, subject))
                {
                    impersonateSubject(filterChain, request, response, subject);
                }
                else
                {
                    actAsAddOnUser(filterChain, request, response, addOnKey);
                }
            }
            catch (InvalidSubjectException e)
            {
                log.error("The subject '{}' is invalid (see above for details) and this request is being stopped by the {}.", e.getMessage(), ThreeLeggedAuthFilter.class.getSimpleName());
            }
            finally
            {
                if (session != null)
                {
                    session.invalidate();
                }
            }
        }
    }

    private void rejectEmptyStringSubject(HttpServletRequest request, HttpServletResponse response, String addOnKey, String subject)
    {
        String externallyVisibleMessage = String.format(MSG_FORMAT_NOT_ALLOWING_IMPERSONATION, addOnKey, subject);
        log.warn("{} because an empty-string username is nonsensical and may indicate a programming error in the add-on.", externallyVisibleMessage);
        fail(request, response, externallyVisibleMessage, HttpServletResponse.SC_BAD_REQUEST);
    }

    private boolean shouldAllowImpersonation(HttpServletRequest request, HttpServletResponse response, String addOnKey, String subject) throws InvalidSubjectException
    {
        boolean allowImpersonation = false;

        if (getBoolean(SYS_PROP_ALLOW_IMPERSONATION))
        {
            log.warn("Allowing add-on '{}' to impersonate user '{}' because the system property '{}' is set to true.", new String[]{ addOnKey, subject, SYS_PROP_ALLOW_IMPERSONATION });
            getUserKey(request, response, addOnKey, subject);
            allowImpersonation = true;
        }
        else
        {
            ConnectAddonBean addOnBean = connectAddonManager.getExistingAddon(addOnKey);

            if (threeLeggedAuthService.shouldSilentlyIgnoreUserAgencyRequest(subject, addOnBean))
            {
                log.warn("Ignoring subject claim '{}' on incoming request '{}' from Connect add-on '{}' because the {} said so.",
                        new String[]{subject, request.getRequestURI(), addOnKey, threeLeggedAuthService.getClass().getSimpleName()});
            }
            else
            {
                final UserKey userKey = getUserKey(request, response, addOnKey, subject);

                // a valid grant must exist
                if (threeLeggedAuthService.hasGrant(userKey, addOnBean))
                {
                    log.info("Allowing add-on '{}' to impersonate user '{}' because a user-agent grant exists.", addOnKey, subject);
                    allowImpersonation = true;
                }
                else
                {
                    String externallyVisibleMessage = String.format(MSG_FORMAT_NOT_ALLOWING_IMPERSONATION, addOnKey, subject);
                    log.warn("{} because this user has not granted user-agent rights to this add-on, or the grant has expired.", externallyVisibleMessage);
                    fail(request, response, externallyVisibleMessage, HttpServletResponse.SC_FORBIDDEN);
                    throw new InvalidSubjectException(subject);
                }
            }
        }

        return allowImpersonation;
    }

    private void impersonateSubject(FilterChain filterChain, HttpServletRequest request, HttpServletResponse response, String subject) throws IOException, ServletException
    {
        final Authenticator.Result authenticationResult = new Authenticator.Result.Success(createMessage("Successful three-legged-auth"), new SimplePrincipal(subject));
        authenticationListener.authenticationSuccess(authenticationResult, request, response);
        filterChain.doFilter(request, response);
    }

    private void actAsAddOnUser(FilterChain filterChain, HttpServletRequest request, HttpServletResponse response, String addOnKey) throws IOException, ServletException
    {
        try
        {
            final Principal principalFromApplink = getPrincipalFromApplink(addOnKey);
            final Authenticator.Result authenticationResult = new Authenticator.Result.Success(createMessage("Successful two-legged-auth"), principalFromApplink);
            authenticationListener.authenticationSuccess(authenticationResult, request, response);
            filterChain.doFilter(request, response);
        }
        catch (InvalidSubjectException e)
        {
            createAndSendFailure(e, response, HttpServletResponse.SC_UNAUTHORIZED, badCredentialsMessage);
        }
    }

    // a null return value means that a failure response has been returned
    private UserKey getUserKey(HttpServletRequest request, HttpServletResponse response, String addOnKey, String subject) throws InvalidSubjectException
    {
        final User user = crowdService.getUser(subject);

        // the user must exist
        if (null == user)
        {
            String externallyVisibleMessage = String.format(MSG_FORMAT_NOT_ALLOWING_IMPERSONATION, addOnKey, subject);
            log.warn("{} because the crowd service says that there is no user with this username.", externallyVisibleMessage);
            fail(request, response, externallyVisibleMessage, HttpServletResponse.SC_UNAUTHORIZED);
            throw new InvalidSubjectException(subject);
        }
        else
        {
            // no impersonating an inactive user; no zombies
            if (user.isActive())
            {
                UserProfile userProfile = userManager.getUserProfile(user.getName());

                if (null == userProfile)
                {
                    // if this ever happens then our internal libs disagree on what is a user and what is not
                    throw new RuntimeException(String.format("The Crowd service said that user '%s' exists but the SAL user manager cannot find a profile.", user.getName()));
                }

                return userProfile.getUserKey();
            }
            else
            {
                String externallyVisibleMessage = String.format(MSG_FORMAT_NOT_ALLOWING_IMPERSONATION, addOnKey, subject);
                log.debug("{} because the crowd service says that this user is inactive.", externallyVisibleMessage);
                fail(request, response, externallyVisibleMessage, HttpServletResponse.SC_UNAUTHORIZED);
                throw new InvalidSubjectException(subject);
            }
        }
    }

    private void fail(HttpServletRequest request, HttpServletResponse response, String externallyVisibleMessage, int httpResponseCode)
    {
        sendErrorResponse(response, httpResponseCode, externallyVisibleMessage);
        authenticationListener.authenticationFailure(new Authenticator.Result.Failure(createMessage("")), request, response);
    }

    @Override
    public void destroy()
    {
    }

    private Principal getPrincipalFromApplink(String jwtIssuer) throws InvalidSubjectException
    {
        Principal userPrincipal = null; // default to being able to see only public resources

        ApplicationLink applicationLink = jwtApplinkFinder.find(jwtIssuer);

        if (null == applicationLink)
        {
            log.warn("Found no application link for JWT issuer '{}'. Incoming requests from this issuer will be authenticated as an anonymous request.", jwtIssuer);
        }
        else
        {
            Object addOnUserKey = applicationLink.getProperty(JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME);

            if (null == addOnUserKey)
            {
                log.warn(String.format("Application link '%s' for JWT issuer '%s' has no '%s' property. Incoming requests from this issuer will be authenticated as an anonymous request.",
                        applicationLink.getId(), jwtIssuer, JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME));
            }
            else
            {
                if (addOnUserKey instanceof String)
                {
                    String userKeyString = (String) addOnUserKey;
                    User user = crowdService.getUser(userKeyString);

                    // if the add-on's user has been disabled then we explicitly deny access so that admins and our add-on
                    // lifecycle code can instantly prevent an add-on from making any calls (e.g. when an add-on is disabled)
                    if (null == user)
                    {
                        throw new InvalidSubjectException(String.format("The user '%s' does not exist", userKeyString));
                    }
                    else if (!user.isActive())
                    {
                        throw new InvalidSubjectException(String.format("The user '%s' is inactive", userKeyString));
                    }

                    userPrincipal = new SimplePrincipal(userKeyString);
                }
                else
                {
                    throw new IllegalStateException(String.format("ApplicationLink '%s' for JWT issuer '%s' has the non-String user key '%s'. The user key must be a String: please correct it by editing the database or, if the issuer is a Connect add-on, by re-installing it.",
                            applicationLink.getId(), jwtIssuer, addOnUserKey));
                }
            }
        }

        return userPrincipal;
    }

    private static Message createMessage(final String message)
    {
        return new DefaultMessage(message);
    }

    private static Authenticator.Result.Failure createAndSendFailure(Exception e, HttpServletResponse response, int httpResponseCode, String externallyVisibleMessage)
    {
        log.debug("Failure during JWT authentication: ", e);
        sendErrorResponse(response, httpResponseCode, externallyVisibleMessage);
        return new Authenticator.Result.Failure(createMessage(e.getLocalizedMessage()));
    }

    private static void sendErrorResponse(HttpServletResponse response, int httpResponseCode, String externallyVisibleMessage)
    {
        response.reset();

        try
        {
            response.sendError(httpResponseCode, externallyVisibleMessage);
        }
        catch (IOException doubleFacePalm)
        {
            log.error("Encountered IOException while trying to report an authentication failure.", doubleFacePalm);

            try
            {
                response.reset();
                response.setStatus(httpResponseCode); // no error message, but hopefully the response code will still be useful
            }
            catch (IllegalStateException cannotReset)
            {
                // Sigh... I tried.
            }
        }
    }
}
