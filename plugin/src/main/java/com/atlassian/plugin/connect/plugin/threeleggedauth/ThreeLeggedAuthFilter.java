package com.atlassian.plugin.connect.plugin.threeleggedauth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.jwt.core.http.auth.SimplePrincipal;
import com.atlassian.jwt.exception.JwtUserRejectedException;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonBeanFactory;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.sal.api.auth.AuthenticationListener;
import com.atlassian.sal.api.auth.Authenticator;
import com.atlassian.sal.api.message.Message;
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
import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;

import static com.atlassian.jwt.JwtConstants.AppLinks.SYS_PROP_ALLOW_IMPERSONATION;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Boolean.getBoolean;

@Component
public class ThreeLeggedAuthFilter implements Filter
{
    private final ThreeLeggedAuthService threeLeggedAuthService;
    private final ConnectAddonRegistry connectAddonRegistry;
    private final ConnectAddonBeanFactory connectAddonBeanFactory;
    private final UserManager userManager;
    private final AuthenticationListener authenticationListener;
    private final JwtApplinkFinder jwtApplinkFinder;
    private final CrowdService crowdService;

    private final static Logger log = LoggerFactory.getLogger(ThreeLeggedAuthFilter.class);
    private static final String BAD_CREDENTIALS_MESSAGE = "Your presented credentials do not provide access to this resource."; // protect against phishing by not saying whether the add-on, user or secret was wrong

    @Autowired
    public ThreeLeggedAuthFilter(ThreeLeggedAuthService threeLeggedAuthService,
                                 ConnectAddonRegistry connectAddonRegistry,
                                 ConnectAddonBeanFactory connectAddonBeanFactory,
                                 UserManager userManager,
                                 AuthenticationListener authenticationListener,
                                 JwtApplinkFinder jwtApplinkFinder,
                                 CrowdService crowdService)
    {
        this.threeLeggedAuthService = checkNotNull(threeLeggedAuthService);
        this.connectAddonRegistry = checkNotNull(connectAddonRegistry);
        this.connectAddonBeanFactory = checkNotNull(connectAddonBeanFactory);
        this.userManager = checkNotNull(userManager);
        this.authenticationListener = checkNotNull(authenticationListener);
        this.jwtApplinkFinder = checkNotNull(jwtApplinkFinder);
        this.crowdService = checkNotNull(crowdService);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        Object addOnKeyObject = servletRequest.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME);
        String addOnKey = addOnKeyObject instanceof String ? (String)addOnKeyObject : null;

        // potentially reject only if the request comes from an add-on
        if (!StringUtils.isEmpty(addOnKey))
        {
            boolean allowImpersonation = false;
            Object subjectObject = servletRequest.getAttribute(JwtConstants.HttpRequests.JWT_SUBJECT_ATTRIBUTE_NAME);
            String subject = subjectObject instanceof String ? (String) subjectObject : null;

            // potentially reject only if the add-on is attempting to act on behalf of a user
            if (!StringUtils.isEmpty(subject))
            {
                boolean abort = true;

                if (getBoolean(SYS_PROP_ALLOW_IMPERSONATION))
                {
                    log.warn("Allowing add-on '{}' to impersonate user '{}' because the system property '{}' is set to true.", new String[]{ addOnKey, subject, SYS_PROP_ALLOW_IMPERSONATION });
                    allowImpersonation = true;
                    abort = false;
                }
                else
                {
                    ConnectAddonBean addOnBean = connectAddonBeanFactory.fromJsonSkipValidation(connectAddonRegistry.getDescriptor(addOnKey));

                    // potentially reject only if the add-on has the scope that allows user-agency
                    if (addOnBean.getScopes().contains(ScopeName.AGENT))
                    {
                        final UserProfile userProfile = userManager.getUserProfile(subject);

                        // the user must exist
                        if (null == userProfile)
                        {
                            log.debug("NOT allowing add-on '{}' to impersonate user '{}' because there is no user profile for that username.", addOnKey, subject);
                        }
                        else
                        {
                            // a valid grant must exist
                            if (threeLeggedAuthService.hasGrant(userProfile.getUserKey(), addOnBean))
                            {
                                log.warn("Allowing add-on '{}' to impersonate user '{}' because there is an access-token showing that this user has authorised this add-on to act on their behalf.",
                                        new String[]{addOnKey, subject});
                                allowImpersonation = true;
                                abort = false;
                            }
                            else
                            {
                                log.warn("NOT allowing add-on '{}' to impersonate user '{}' because this user has not granted user-agent rights to this add-on, or the grant has expired.",
                                        new Object[]{addOnKey, subject});
                            }
                        }
                    }
                    else
                    {
                        log.warn("Ignoring subject claim '{}' on incoming request '{}' from Connect add-on '{}' because the add-on does not have the '{}' scope.",
                                new String[]{subject, request.getRequestURI(), addOnKey, ScopeName.AGENT.toString()});
                        abort = false;
                    }
                }

                if (abort)
                {
                    return;
                }
            }

            if (allowImpersonation)
            {
                final Authenticator.Result authenticationResult = new Authenticator.Result.Success(createMessage("Successful three-legged-auth"), new SimplePrincipal(subject));
                authenticationListener.authenticationSuccess(authenticationResult, request, response);
            }
            else
            {
                try
                {
                    final Principal principalFromApplink = getPrincipalFromApplink(addOnKey);
                    final Authenticator.Result authenticationResult = new Authenticator.Result.Success(createMessage("Successful two-legged-auth"), principalFromApplink);
                    authenticationListener.authenticationSuccess(authenticationResult, request, response);
                }
                catch (JwtUserRejectedException e)
                {
                    createAndSendFailure(e, response, HttpServletResponse.SC_UNAUTHORIZED, BAD_CREDENTIALS_MESSAGE);
                    return;
                }
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy()
    {
    }

    private Principal getPrincipalFromApplink(String jwtIssuer) throws JwtUserRejectedException
    {
        Principal userPrincipal = null; // default to being able to see only public resources

        ApplicationLink applicationLink = jwtApplinkFinder.find(jwtIssuer);
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
                    throw new JwtUserRejectedException(String.format("The user '%s' does not exist", userKeyString));
                }
                else if (!user.isActive())
                {
                    throw new JwtUserRejectedException(String.format("The user '%s' is inactive", userKeyString));
                }

                userPrincipal = new SimplePrincipal(userKeyString);
            }
            else
            {
                throw new IllegalStateException(String.format("ApplicationLink '%s' for JWT issuer '%s' has the non-String user key '%s'. The user key must be a String: please correct it by editing the database or, if the issuer is a Connect add-on, by re-installing it.",
                        applicationLink.getId(), jwtIssuer, addOnUserKey));
            }
        }

        return userPrincipal;
    }

    private static Message createMessage(final String message)
    {
        return new Message()
        {
            @Override
            public String getKey()
            {
                return message;
            }

            @Override
            public Serializable[] getArguments()
            {
                return null;
            }

            @Override
            public String toString()
            {
                return message;
            }
        };
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
            response.reset();
            response.setStatus(httpResponseCode); // no error message, but hopefully the response code will still be useful
        }
    }
}
