package com.atlassian.plugin.connect.crowd.permissions;

import java.util.Arrays;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.crowd.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.sal.api.net.Request.MethodType;
import com.atlassian.sal.api.net.ResponseException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestConnectCrowdPermissionsClientImpl
{
    @Mock
    private ConnectCrowdSysadminHttpClient connectCrowdSysadminHttpClient;
    private ConnectCrowdPermissionsClientImpl connectCrowdPermissionsClient;

    @Before
    public void beforeEach()
    {
        initMocks(this);
        connectCrowdPermissionsClient = new ConnectCrowdPermissionsClientImpl(connectCrowdSysadminHttpClient);
    }

    @Test
    public void grantAdminPermissionReturnsFalseIfExceptionThrown()
            throws Exception
    {
        for (Exception exception: Arrays.asList(
                new CredentialsRequiredException(null, null),
                new InvalidAuthenticationException((Throwable)null),
                new OperationFailedException(),
                new ApplicationAccessDeniedException(),
                new InactiveAccountException(null),
                new ApplicationPermissionException(),
                new ResponseException()
        ))
        {
            makeRequestMethodThrow(exception);
            assertThat(connectCrowdPermissionsClient.grantAdminPermission("group-name", "product-id", "application-id"), is(false));
        }
    }

    private void makeRequestMethodThrow(Exception exception)
    {
        try
        {
            doThrow(exception).when(connectCrowdSysadminHttpClient).executeAsSysadmin(any(MethodType.class), anyString(), anyString());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}