package com.atlassian.plugin.connect.plugin.threeleggedauth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.jwt.core.http.auth.SimplePrincipal;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.plugin.util.DefaultMessage;
import com.atlassian.plugin.connect.spi.user.ConnectUserService;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.auth.AuthenticationListener;
import com.atlassian.sal.api.auth.Authenticator;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.atlassian.jwt.JwtConstants.AppLinks.SYS_PROP_ALLOW_IMPERSONATION;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Boolean.getBoolean;

@Component
@ExportAsService(LifecycleAware.class)
public class ThreeLeggedAuthFilter implements Filter, LifecycleAware
{
    private final ThreeLeggedAuthService threeLeggedAuthService;
    private final ConnectAddonManager connectAddonManager;
    private final AuthenticationListener authenticationListener;
    private final JwtApplinkFinder jwtApplinkFinder;
    private final UserManager userManager;
    private final ConnectUserService userService;
    private final String badCredentialsMessage; // protect against phishing by not saying whether the add-on, user or secret was wrong

    private final static Logger log = LoggerFactory.getLogger(ThreeLeggedAuthFilter.class);
    private static final String MSG_FORMAT_NOT_ALLOWING_IMPERSONATION = "Add-on '%s' disallowed to impersonate user '%s'";
    private AtomicBoolean started = new AtomicBoolean(false);

    @Autowired
    public ThreeLeggedAuthFilter(ThreeLeggedAuthService threeLeggedAuthService,
                                 ConnectAddonManager connectAddonManager,
                                 UserManager userManager,
                                 ConnectUserService userService,
                                 AuthenticationListener authenticationListener,
                                 JwtApplinkFinder jwtApplinkFinder,
                                 I18nResolver i18nResolver)
    {
        this.threeLeggedAuthService = checkNotNull(threeLeggedAuthService, "threeLeggedAuthService");
        this.connectAddonManager = checkNotNull(connectAddonManager, "connectAddOnManager");
        this.userManager = checkNotNull(userManager, "userManager");
        this.userService = checkNotNull(userService, "userService");
        this.authenticationListener = checkNotNull(authenticationListener, "authenticationListener");
        this.jwtApplinkFinder = checkNotNull(jwtApplinkFinder, "jwtApplinkFinder");
        this.badCredentialsMessage = i18nResolver.getText("connect.3la.bad_credentials");
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

        // This plugin now run in phase 1. So, there is case that the filter is called before application starts.
        // And as the filter need to access the tenant info, it should not process before application starts.
        if (!started.get()) {
            log.debug("Application has not started yet, filter skipped.");
            filterChain.doFilter(request, response);
            return;
        }

        String addOnKey = getAddonKeyFromRequest(servletRequest);

        if (!StringUtils.isEmpty(addOnKey))
        {
            final ConnectAddonBean addonBean = connectAddonManager.getExistingAddon(addOnKey);
            if (isJsonJwtAddon(addonBean))
            {
                processAddOnRequest(filterChain, request, response, addonBean);
            }
            else
            {
                filterChain.doFilter(request, response);
            }
        }
        else
        {
            filterChain.doFilter(request, response);
        }
    }

    @Override
    public void onStart()
    {
        log.debug("Application started.");
        started.set(true);
    }

    @Override
    public void onStop()
    {
        started.set(false);
    }

    private String getAddonKeyFromRequest(ServletRequest servletRequest)
    {
        Object addOnKeyObject = servletRequest.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME);
        String addOnKey = addOnKeyObject instanceof String ? (String)addOnKeyObject : null;

        // warn if weird properties are set
        if (null != addOnKeyObject && !(addOnKeyObject instanceof String))
        {
            log.warn("The value of the request attribute '{}' should be a string but instead it is a '{}': '{}'. This is a programming error in the code that sets this value.",
                    new Object[]{JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME, addOnKeyObject.getClass().getSimpleName(), addOnKeyObject});
        }
        return addOnKey;
    }

    private boolean isJsonJwtAddon(ConnectAddonBean addonBean)
    {
        return addonBean != null && addonBean.getAuthentication() != null && addonBean.getAuthentication().getType() == AuthenticationType.JWT;
    }

    private void processAddOnRequest(FilterChain filterChain, HttpServletRequest request, HttpServletResponse response, ConnectAddonBean addOnBean) throws IOException, ServletException
    {
        Object subjectObject = request.getAttribute(JwtConstants.HttpRequests.JWT_SUBJECT_ATTRIBUTE_NAME);
        String subject = subjectObject instanceof String ? (String) subjectObject : null;

        // an empty-string subject claim would be nonsensical and may indicate a programming error in the add-on
        if ("".equals(subject))
        {
            rejectEmptyStringSubject(request, response, addOnBean.getKey(), subject);
        }
        else
        {
            // ACDEV-1304: Ensure that any add-on requests have no effect on a session that may already exist.
            HttpSession session = request.getSession(false); // don't create a session if there is none
            try
            {
                UserProfile impersonatedUserProfile = null;
                if (null != subject)
                {
                    impersonatedUserProfile = getUserProfileIfImpersonationAllowed(request, response, subject, addOnBean);
                }

                if (impersonatedUserProfile != null)
                {
                    impersonateSubject(filterChain, request, response, impersonatedUserProfile);
                }
                else
                {
                    actAsAddOnUser(filterChain, request, response, addOnBean.getKey());
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

    /**
     * @return Return the user profile to impersonate with if allowed, otherwise null
     */
    private UserProfile getUserProfileIfImpersonationAllowed(HttpServletRequest request, HttpServletResponse response, String subject, ConnectAddonBean addOnBean) throws InvalidSubjectException
    {
        if (getBoolean(SYS_PROP_ALLOW_IMPERSONATION))
        {
            log.info("Allowing add-on '{}' to impersonate user '{}' because the system property '{}' is set to true.", new String[]{ addOnBean.getKey(), subject, SYS_PROP_ALLOW_IMPERSONATION });
            return getUserProfile(request, response, addOnBean.getKey(), subject);
        }
        else
        {
            if (threeLeggedAuthService.shouldSilentlyIgnoreUserAgencyRequest(subject, addOnBean))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Ignoring subject claim '{}' on incoming request '{}' from Connect add-on '{}' because the {} said so.",
                            new String[] { subject, request.getRequestURI(), addOnBean.getKey(), threeLeggedAuthService.getClass().getSimpleName() });
                }
                return null;
            }
            else
            {
                final UserProfile userProfile = getUserProfile(request, response, addOnBean.getKey(), subject);

                // a valid grant must exist
                if (threeLeggedAuthService.hasGrant(userProfile.getUserKey(), addOnBean))
                {
                    log.info("Allowing add-on '{}' to impersonate user '{}' because a user-agent grant exists.", addOnBean.getKey(), subject);
                    return userProfile;
                }
                else
                {
                    String externallyVisibleMessage = String.format(MSG_FORMAT_NOT_ALLOWING_IMPERSONATION, addOnBean.getKey(), subject);
                    log.debug("{} because this user has not granted user-agent rights to this add-on, or the grant has expired.", externallyVisibleMessage);
                    fail(request, response, externallyVisibleMessage, HttpServletResponse.SC_FORBIDDEN);
                    throw new InvalidSubjectException(subject);
                }
            }
        }
    }

    private void impersonateSubject(FilterChain filterChain, HttpServletRequest request, HttpServletResponse response, UserProfile userProfile) throws IOException, ServletException
    {
        // Products use the username to set the authentication context.
        SimplePrincipal principal = new SimplePrincipal(userProfile.getUsername());
        final Authenticator.Result authenticationResult = new Authenticator.Result.Success(createMessage("Successful three-legged-auth"), principal);
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
    private UserProfile getUserProfile(HttpServletRequest request, HttpServletResponse response, String addOnKey, String subject) throws InvalidSubjectException
    {
        UserKey userKey = new UserKey(subject);
        UserProfile userProfile = userManager.getUserProfile(userKey);

        if (null == userProfile)
        {
            String externallyVisibleMessage = String.format(MSG_FORMAT_NOT_ALLOWING_IMPERSONATION, addOnKey, subject);
            log.warn("{} because we can't find a user with this user key.", externallyVisibleMessage);
            fail(request, response, externallyVisibleMessage, HttpServletResponse.SC_UNAUTHORIZED);
            throw new InvalidSubjectException(subject);
        }
        else
        {
            if (!userService.isActive(userProfile.getUsername()))
            {
                String externallyVisibleMessage = String.format(MSG_FORMAT_NOT_ALLOWING_IMPERSONATION, addOnKey, subject);
                log.debug("{} because the crowd service says that this user is inactive.", externallyVisibleMessage);
                fail(request, response, externallyVisibleMessage, HttpServletResponse.SC_UNAUTHORIZED);
                throw new InvalidSubjectException(subject);
            }
        }
        return userProfile;
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
            // the AppLinks constant is "user.key", but it is used to store the username. Confusing, but for backwards compatibility we're retaining
            // the constant name
            Object addOnUsername = applicationLink.getProperty(JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME);

            if (null == addOnUsername)
            {
                log.warn(String.format("Application link '%s' for JWT issuer '%s' has no '%s' property. Incoming requests from this issuer will be authenticated as an anonymous request.",
                        applicationLink.getId(), jwtIssuer, JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME));
            }
            else
            {
                if (addOnUsername instanceof String)
                {
                    String username = (String) addOnUsername;
                    if (!userService.isActive(username))
                    {
                        throw new InvalidSubjectException(String.format("The user '%s' is inactive", username));
                    }

                    userPrincipal = new SimplePrincipal(username);
                }
                else
                {
                    throw new IllegalStateException(String.format("ApplicationLink '%s' for JWT issuer '%s' has the non-String user key '%s'. The user key must be a String: please correct it by editing the database or, if the issuer is a Connect add-on, by re-installing it.",
                            applicationLink.getId(), jwtIssuer, addOnUsername));
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
