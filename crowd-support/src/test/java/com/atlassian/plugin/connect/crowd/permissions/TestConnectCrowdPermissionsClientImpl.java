package com.atlassian.plugin.connect.crowd.permissions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.crowd.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.sal.api.net.Request.MethodType;
import com.atlassian.sal.api.net.ResponseException;

import com.google.common.collect.ImmutableMap;

import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import static com.atlassian.sal.api.net.Request.MethodType.POST;
import static com.atlassian.sal.api.net.Request.MethodType.PUT;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestConnectCrowdPermissionsClientImpl
{
    @Captor
    private ArgumentCaptor<String> dataCaptor;

    @Mock
    private ConnectCrowdSysadminHttpClient connectCrowdSysadminHttpClient;

    public static final String ACCESS_CONFIG_URL = "/rest/um/1/accessconfig/group?productId=product%3Ajira%3Ajira";
    public static final String HOST_ACCESS_CONFIG_URL = "/rest/um/1/accessconfig/group?hostId=jira&productId=product%3Ajira%3Ajira";
    public static final Map<String, Object> expectedConfigData = ImmutableMap.<String, Object>of(
            "name", "atlassian-addons-admin",
            "use", "NONE",
            "admin", "DIRECT",
            "defaultUse", false
    );

    private ConnectCrowdPermissionsClientImpl connectCrowdPermissionsClient;

    @Before
    public void beforeEach()
    {
        initMocks(this);
        connectCrowdPermissionsClient = new ConnectCrowdPermissionsClientImpl(connectCrowdSysadminHttpClient);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void grantAdminPermissionPostsAdminsGroupToAccessConfigEndpoint()
            throws Exception
    {
        connectCrowdPermissionsClient.grantAdminPermission("group-name");

        verify(connectCrowdSysadminHttpClient).executeAsSysadmin(eq(POST), eq(ACCESS_CONFIG_URL), dataCaptor.capture());
        final List<String> providedData = (List<String>) new JSONParser().parse(dataCaptor.getValue());
        assertThat(providedData, is(singletonList("atlassian-addons-admin")));
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void grantAdminPermissionPutsAdminConfigToAccesConfigEndpoint()
            throws Exception
    {
        connectCrowdPermissionsClient.grantAdminPermission("group-name");

        verify(connectCrowdSysadminHttpClient).executeAsSysadmin(eq(PUT), eq(HOST_ACCESS_CONFIG_URL), dataCaptor.capture());
        final Map<String, Object> providedData = (Map<String, Object>) new JSONParser().parse(dataCaptor.getValue());
        assertThat(providedData, is(expectedConfigData));
    }

    @Test
    public void grantAdminPermissionReturnsFalseIfExceptionThrown()
            throws Exception
    {
        for (Exception exception : Arrays.asList(
                new CredentialsRequiredException(null, null),
                new InvalidAuthenticationException((Throwable) null),
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