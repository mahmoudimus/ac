package com.atlassian.plugin.connect.crowd.usermanagement;

import java.util.Set;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserProvisioningService;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil;
import com.atlassian.plugin.connect.crowd.usermanagement.api.ConnectCrowdService;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.host.HostProperties;
import com.atlassian.plugin.connect.spi.user.ConnectAddOnUserService;
import com.atlassian.plugin.connect.spi.usermanagment.ConnectAddOnUserDisableException;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil.Constants;
import static com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil.buildConnectAddOnUserAttribute;
import static com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil.usernameForAddon;
import static com.google.common.base.Preconditions.checkNotNull;

@ExportAsDevService
@ConfluenceComponent
@JiraComponent
public class CrowdAddOnUserService implements ConnectAddOnUserService
{
    // This password prevents logging in - we can't send remote crowd the pre-encrypted version, though
    public static final PasswordCredential PREVENT_LOGIN = PasswordCredential.unencrypted("X");
    private final ConnectAddOnUserProvisioningService connectAddOnUserProvisioningService;
    private final ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService;
    private static final Logger log = LoggerFactory.getLogger(CrowdAddOnUserService.class);
    private final ConnectCrowdService connectCrowdService;
    private final HostProperties hostProperties;

    @Autowired
    public CrowdAddOnUserService(ConnectAddOnUserProvisioningService connectAddOnUserProvisioningService,
            ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService, ConnectCrowdService connectCrowdService,
            HostProperties hostProperties)
    {
        this.connectCrowdService = connectCrowdService;
        this.hostProperties = hostProperties;
        this.connectAddOnUserProvisioningService = checkNotNull(connectAddOnUserProvisioningService);
        this.connectAddOnUserGroupProvisioningService = checkNotNull(connectAddOnUserGroupProvisioningService);
    }

    @Override
    public String getOrCreateUserName(String addOnKey, String addOnDisplayName) throws ConnectAddOnUserInitException
    {
        try
        {
            return createOrEnableAddOnUser(ConnectAddOnUserUtil.usernameForAddon(checkNotNull(addOnKey)), checkNotNull(addOnDisplayName));
        }
        catch ( ApplicationPermissionException
                | UserNotFoundException
                | GroupNotFoundException
                | ApplicationNotFoundException
                | OperationFailedException
                | InvalidAuthenticationException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
    }

    @Override
    public void disableAddonUser(String addOnKey) throws ConnectAddOnUserDisableException
    {
        connectCrowdService.disableUser(usernameForAddon(addOnKey));
    }

    @Override
    public String provisionAddonUserForScopes(String addOnKey, String addOnDisplayName, Set<ScopeName> previousScopes, Set<ScopeName> newScopes) throws ConnectAddOnUserInitException
    {
        String username = getOrCreateUserName(checkNotNull(addOnKey), checkNotNull(addOnDisplayName));
        connectAddOnUserProvisioningService.provisionAddonUserForScopes(username, previousScopes, newScopes);
        return username;
    }

    private String createOrEnableAddOnUser(String username, String addOnDisplayName)
            throws ApplicationNotFoundException, OperationFailedException, ApplicationPermissionException, UserNotFoundException, GroupNotFoundException, InvalidAuthenticationException
    {
        connectAddOnUserGroupProvisioningService.ensureGroupExists(Constants.ADDON_USER_GROUP_KEY);
        User user = connectCrowdService.createOrEnableUser(username, addOnDisplayName, Constants.ADDON_USER_EMAIL_ADDRESS, PREVENT_LOGIN, buildConnectAddOnUserAttribute(hostProperties.getKey()));

        connectAddOnUserGroupProvisioningService.ensureUserIsInGroup(user.getName(), Constants.ADDON_USER_GROUP_KEY);

        for (String group : connectAddOnUserProvisioningService.getDefaultProductGroupsAlwaysExpected())
        {
            try
            {
                connectAddOnUserGroupProvisioningService.ensureUserIsInGroup(user.getName(), group);
            }
            catch (GroupNotFoundException e)
            {
                // carry on if the group does not exist so that an admin deleting a group will not kill all add-on installations
                log.error(String.format("Could not make user '%s' a member of group '%s' because that group does not exist! " +
                        "The user needs to be a member of this group, otherwise the add-on will not function correctly. " +
                        "Please check with an instance administrator that this group exists and that it is not read-only.", username, group));
                // TODO ACDEV-938: propagate this error
            }
        }

        int numPossibleDefaultGroupsAddedTo = 0;
        String errorMessage = String.format("Could not make user '%s' a member of one of groups ", username);
        for (String group : connectAddOnUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected())
        {
            try
            {
                connectAddOnUserGroupProvisioningService.ensureUserIsInGroup(user.getName(), group);
                numPossibleDefaultGroupsAddedTo++;
            }
            catch (GroupNotFoundException e)
            {
                errorMessage += String.format("%s, ", group);
            }

        }
        if (numPossibleDefaultGroupsAddedTo == 0 && connectAddOnUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected().size() > 0)
        {
            log.error(errorMessage + "because none of those groups exist!" +
                    "We expect at least one of these groups to exist - exactly which one should exist depends on the version of the instance." +
                    "The user needs to be a member of one of these groups for basic access, otherwise the add-on will not function correctly." +
                    "Please check with an instance administrator that at least one of these groups exists and that it is not read-only.");
        }

        return user.getName();
    }
}

