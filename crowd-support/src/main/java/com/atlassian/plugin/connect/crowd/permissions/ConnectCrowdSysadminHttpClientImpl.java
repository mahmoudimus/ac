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
import com.atlassian.plugin.connect.crowd.usermanagement.CrowdClientProvider;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.ResponseException;
import com.google.common.collect.Iterables;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.HttpCookie;
import java.util.Optional;

@JiraComponent
public class ConnectCrowdSysadminHttpClientImpl
        implements ConnectCrowdSysadminHttpClient {
    private final ApplicationLinkService applicationLinkService;
    private final CrowdClientProvider crowdClientProvider;

    @Autowired
    public ConnectCrowdSysadminHttpClientImpl(
            ApplicationLinkService applicationLinkService,
            CrowdClientProvider crowdClientProvider) {
        this.applicationLinkService = applicationLinkService;
        this.crowdClientProvider = crowdClientProvider;
    }

    // This code is adapted from com.atlassian.usermanagement.client.impl.UserManagementRequestServiceImpl#executeAsSysadmin
    // in the https://stash.atlassian.com/projects/UN/repos/user-management repo.
    // https://ecosystem.atlassian.net/browse/ACDEV-2237 has been raised to move to a stable means of granting admin permission
    // provided by the user management plugin, once one becomes available.
    public void executeAsSysadmin(Request.MethodType methodType, String url, String jsonString)
            throws CredentialsRequiredException, ResponseException,
            ApplicationPermissionException, InactiveAccountException,
            ApplicationAccessDeniedException, OperationFailedException,
            InvalidAuthenticationException {
        Optional<ApplicationLink> possibleCrowd = Optional.ofNullable(Iterables.getFirst(applicationLinkService.getApplicationLinks(CrowdApplicationType.class), null));
        if (!possibleCrowd.isPresent()) {
            throw new OperationFailedException("There was no Crowd application link. This is a problem");
        }
        ReadOnlyApplicationLink crowd = possibleCrowd.get();
        ApplicationLinkRequest request = crowd.createAuthenticatedRequestFactory().createRequest(methodType, url);
        request.addHeader("Cookie", generateSysadminCookie().toString());
        request.addHeader("Content-Type", "application/json");
        request.setEntity(jsonString);
        request.execute();
    }

    private HttpCookie generateSysadminCookie()
            throws InactiveAccountException, OperationFailedException,
            ApplicationAccessDeniedException, ApplicationPermissionException,
            InvalidAuthenticationException {
        final UserAuthenticationContext sysadminAuthenticationContext = new UserAuthenticationContext("sysadmin",
                null,
                new ValidationFactor[]{}, "jira");
        {
            String ssoToken = crowdClientProvider.getCrowdClient().authenticateSSOUserWithoutValidatingPassword(sysadminAuthenticationContext);
            return new HttpCookie("studio.crowd.tokenkey", ssoToken);
        }
    }
}
