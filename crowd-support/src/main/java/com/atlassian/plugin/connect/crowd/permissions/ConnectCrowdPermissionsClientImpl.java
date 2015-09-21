package com.atlassian.plugin.connect.crowd.permissions;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.crowd.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.sal.api.net.ResponseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.sal.api.net.Request.MethodType.POST;
import static com.atlassian.sal.api.net.Request.MethodType.PUT;

@JiraComponent
public class ConnectCrowdPermissionsClientImpl
        implements ConnectCrowdPermissionsClient
{
    private static final Logger log = LoggerFactory.getLogger(ConnectCrowdPermissionsClientImpl.class);

    private final ConnectCrowdSysadminHttpClient connectCrowdSysadminHttpClient;

    @Autowired
    public ConnectCrowdPermissionsClientImpl(ConnectCrowdSysadminHttpClient connectCrowdSysadminHttpClient)
    {
        this.connectCrowdSysadminHttpClient = connectCrowdSysadminHttpClient;
    }

    @Override
    public boolean grantAdminPermission(String groupName, String productId, String applicationId)
    {
        try
        {
            connectCrowdSysadminHttpClient.executeAsSysadmin(POST,
                    String.format("/rest/um/1/accessconfig/group?productId=product%3A{}%3A{}", productId, applicationId), String.format("[\"{}\"]", groupName));
            connectCrowdSysadminHttpClient.executeAsSysadmin(PUT,
                    String.format("/rest/um/1/accessconfig/group?hostId={}&productId=product%3A{}%3A{}", productId, productId, applicationId),
                    String.format("{\"name\": \"{}\", \"use\": \"NONE\", \"admin\": \"DIRECT\", \"defaultUse\": false}", groupName));
        }
        catch (InactiveAccountException
                | ApplicationPermissionException
                | ApplicationAccessDeniedException
                | InvalidAuthenticationException
                | OperationFailedException
                | CredentialsRequiredException
                | ResponseException e)
        {
            log.warn("Could not grant remote admin permission to the group '{}' due to the following exception", groupName, e);
            return false;
        }
        return true;
    }
}
