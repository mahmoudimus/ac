package com.atlassian.plugin.connect.crowd.permissions;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.ReadOnlyApplicationLink;
import com.atlassian.applinks.api.application.crowd.CrowdApplicationType;
import com.atlassian.crowd.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.model.authentication.UserAuthenticationContext;
import com.atlassian.crowd.model.authentication.ValidationFactor;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.crowd.usermanagement.CrowdClientProvider;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.ResponseException;

import org.apache.commons.httpclient.Cookie;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.fugue.Iterables.first;
import static com.atlassian.sal.api.net.Request.MethodType.POST;
import static com.atlassian.sal.api.net.Request.MethodType.PUT;

@JiraComponent
public class ConnectCrowdPermissionsClientImpl
        implements ConnectCrowdPermissionsClient
{
    private static final Logger log = LoggerFactory.getLogger(ConnectCrowdPermissionsClientImpl.class);

    private final ApplicationLinkService applicationLinkService;
    private final CrowdClientProvider crowdClientProvider;

    @Autowired
    public ConnectCrowdPermissionsClientImpl(
            ApplicationLinkService applicationLinkService,
            CrowdClientProvider crowdClientProvider)
    {
        this.applicationLinkService = applicationLinkService;
        this.crowdClientProvider = crowdClientProvider;
    }

    @Override
    public boolean grantAdminPermission(String groupName)
    {
        try
        {
            executeAsSysadmin(POST, "/rest/um/1/accessconfig/group?productId=product%3Ajira%3Ajira", "[\"atlassian-addons-admin\"]");
            executeAsSysadmin(PUT, "/rest/um/1/accessconfig/group?hostId=jira&productId=product%3Ajira%3Ajira", "{name: \"atlassian-addons-admin\", use: \"NONE\", admin: \"DIRECT\", defaultUse: false}");
        }
        catch (InactiveAccountException
                | ApplicationPermissionException
                | ApplicationAccessDeniedException
                | InvalidAuthenticationException
                | OperationFailedException
                | CredentialsRequiredException
                | ResponseException e)
        {
            log.warn("Could not grant remote admin permission to the group " + groupName + "due to the following exception", e);
            return false;
        }
        return true;
    }

    private String executeAsSysadmin(Request.MethodType methodType, String url, String jsonString)
            throws CredentialsRequiredException, ResponseException, ApplicationPermissionException, InactiveAccountException, ApplicationAccessDeniedException, OperationFailedException, InvalidAuthenticationException
    {
        Option<ApplicationLink> possibleCrowd = first(applicationLinkService.getApplicationLinks(CrowdApplicationType.class));
        if (possibleCrowd.isEmpty())
        {
            throw new OperationFailedException("There was no Crowd application link. This is a problem");
        }
        ReadOnlyApplicationLink crowd = possibleCrowd.get();
        ApplicationLinkRequest request = crowd.createAuthenticatedRequestFactory().createRequest(methodType, url);
        request.addHeader("Cookie", generateSysadminCookie(crowd.getDisplayUrl().getHost()).toExternalForm());
        request.addHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(jsonString, "UTF-8"));
        return request.execute();
    }

    private Cookie generateSysadminCookie(String host)
            throws InactiveAccountException, OperationFailedException, ApplicationAccessDeniedException, ApplicationPermissionException, InvalidAuthenticationException
    {
        final UserAuthenticationContext sysadminAuthenticationContext = new UserAuthenticationContext("sysadmin",
                null,
                new ValidationFactor[] {}, "jira");
        {
            String ssoToken = crowdClientProvider.getCrowdClient().authenticateSSOUserWithoutValidatingPassword(sysadminAuthenticationContext);
            return new Cookie(host, "studio.crowd.tokenkey", ssoToken);
        }
    }
}
