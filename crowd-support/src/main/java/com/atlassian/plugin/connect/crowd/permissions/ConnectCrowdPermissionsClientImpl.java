package com.atlassian.plugin.connect.crowd.permissions;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.crowd.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.uri.UriBuilder;

import com.google.common.collect.ImmutableMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.sal.api.net.Request.MethodType.POST;
import static com.atlassian.sal.api.net.Request.MethodType.PUT;

@JiraComponent
public class ConnectCrowdPermissionsClientImpl
        implements ConnectCrowdPermissionsClient
{
    public static final String CONFIG_PATH = "/rest/um/1/accessconfig/group";

    private static final Logger log = LoggerFactory.getLogger(ConnectCrowdPermissionsClientImpl.class);

    private final ConnectCrowdSysadminHttpClient connectCrowdSysadminHttpClient;

    @Autowired
    public ConnectCrowdPermissionsClientImpl(ConnectCrowdSysadminHttpClient connectCrowdSysadminHttpClient)
    {
        this.connectCrowdSysadminHttpClient = connectCrowdSysadminHttpClient;
    }

    private String addProductUri(String productId, String applicationId)
    {
        return new UriBuilder().setPath(CONFIG_PATH)
                .addQueryParameter("productId", "product:" + productId + ":" + applicationId).toString();
    }

    private String configureProductUri(String productId, String applicationId)
    {
        return new UriBuilder().setPath(CONFIG_PATH)
                .addQueryParameter("hostId", productId)
                .addQueryParameter("productId", "product:" + productId + ":" + applicationId).toString();
    }

    @Override
    public boolean grantAdminPermission(String groupName, String productId, String applicationId)
    {
        try
        {
            connectCrowdSysadminHttpClient.executeAsSysadmin(POST, addProductUri(productId, applicationId), groupsList(groupName));
            connectCrowdSysadminHttpClient.executeAsSysadmin(PUT, configureProductUri(productId, applicationId), groupData(groupName));
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

    @SuppressWarnings ("unchecked")
    private String groupsList(String groupName)
    {
        final JSONArray jsonArray = new JSONArray();
        jsonArray.add(groupName);
        return jsonArray.toJSONString();
    }

    private String groupData(String groupName)
    {
        return new JSONObject(ImmutableMap.of(
                "name", groupName,
                "use", "NONE",
                "admin", "DIRECT",
                "defaultUse", false
        )).toJSONString();
    }
}
