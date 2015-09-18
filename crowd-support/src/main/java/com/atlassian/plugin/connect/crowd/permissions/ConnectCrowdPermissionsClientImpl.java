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
import com.atlassian.usermanagement.client.api.exception.UserManagementException;

import org.apache.commons.httpclient.Cookie;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.fugue.Iterables.first;
import static com.atlassian.sal.api.net.Request.MethodType.POST;
import static com.atlassian.sal.api.net.Request.MethodType.PUT;

@JiraComponent
public class ConnectCrowdPermissionsClientImpl
        implements ConnectCrowdPermissionsClient
{
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
            executeAsSysadmin(POST, "/admin/rest/um/1/accessconfig/group?productId=product:jira:jira", "[\"atlassian-addons-admin\"]");
            executeAsSysadmin(PUT, "/admin/rest/um/1/accessconfig/group?hostId=jira&productId=product:jira:jira", "{name: \"atlassian-addons-admin\", use: \"NONE\", admin: \"DIRECT\", defaultUse: false}");
        }
        catch (CredentialsRequiredException | UserManagementException | ResponseException e)
        {
            return false;
        }
        return true;
    }

    private String executeAsSysadmin(Request.MethodType methodType, String url, String JSONString)
            throws CredentialsRequiredException, UserManagementException, ResponseException
    {
        Option<ApplicationLink> possibleCrowd = first(applicationLinkService.getApplicationLinks(CrowdApplicationType.class));
        if (possibleCrowd.isEmpty())
        {
            throw new UserManagementException("There was no Crowd application link. This is a problem");
        }
        ReadOnlyApplicationLink crowd = possibleCrowd.get();
        ApplicationLinkRequest request = crowd.createAuthenticatedRequestFactory().createRequest(methodType, url);
        request.addHeader("Cookie", generateSysadminCookie(crowd.getDisplayUrl().getHost()).toExternalForm());
        request.addHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(JSONString, "UTF-8"));
        return request.execute();
    }

    private Cookie generateSysadminCookie(String host)
            throws UserManagementException
    {
        final UserAuthenticationContext sysadminAuthenticationContext = new UserAuthenticationContext("sysadmin",
                null,
                new ValidationFactor[] {}, "jira");

        try
        {
            String ssoToken = crowdClientProvider.getCrowdClient().authenticateSSOUserWithoutValidatingPassword(sysadminAuthenticationContext);
            return new Cookie(host, "studio.crowd.tokenkey", ssoToken);
        }
        catch (ApplicationPermissionException | InactiveAccountException | InvalidAuthenticationException | OperationFailedException | ApplicationAccessDeniedException e)
        {
            throw new UserManagementException("Failed to generate token to communicate with Crowd. This is a problem", e);
        }
    }
}
