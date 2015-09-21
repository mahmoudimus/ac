package com.atlassian.plugin.connect.crowd.permissions;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.crowd.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.ResponseException;

public interface ConnectCrowdSysadminHttpClient
{
    void executeAsSysadmin(Request.MethodType methodType, String url, String jsonString)
            throws CredentialsRequiredException, ResponseException,
            ApplicationPermissionException, InactiveAccountException, ApplicationAccessDeniedException,
            OperationFailedException, InvalidAuthenticationException;
}
