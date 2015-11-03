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

    @Override
    public boolean grantAdminPermission(String groupName, String productId, String applicationId)
    {
        try
        {
            connectCrowdSysadminHttpClient.executeAsSysadmin(POST, addProductUri(productId, applicationId), groupsList(groupName).toJSONString());
            connectCrowdSysadminHttpClient.executeAsSysadmin(PUT, configureProductUri(productId, applicationId), groupData(groupName).toJSONString());
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

    @SuppressWarnings ("unchecked")
    private JSONArray groupsList(String groupName)
    {
        final JSONArray jsonArray = new JSONArray();
        jsonArray.add(groupData(groupName));
        return jsonArray;
    }

    /**
     * @param groupName the name of the group
     * @return a JSON object with the structure of com.atlassian.crowd.plugin.usermanagement.rest.entity.ProductDetailsEntity.GroupEntity
     */
    private JSONObject groupData(String groupName)
    {
        return new JSONObject(ImmutableMap.of(
                "name", groupName,
                "use", "NONE",
                "admin", "DIRECT",
                "defaultUse", false
        ));
    }
}
