package com.atlassian.plugin.connect.crowd.usermanagement;

import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.HostProperties;
import com.atlassian.plugin.connect.spi.lifecycle.ConnectAddonDisableException;
import com.atlassian.plugin.connect.spi.lifecycle.ConnectAddonInitException;
import com.atlassian.plugin.connect.spi.auth.user.ConnectUserService;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.util.concurrent.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddOnUserUtil.Constants;
import static com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddOnUserUtil.addOnRequiresUser;
import static com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddOnUserUtil.buildConnectAddOnUserAttribute;
import static com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddOnUserUtil.usernameForAddon;
import static com.google.common.base.Preconditions.checkNotNull;

@ExportAsDevService
@ConfluenceComponent
@JiraComponent
public class CrowdAddOnUserService implements ConnectUserService
{
    public static final PasswordCredential PREVENT_LOGIN = PasswordCredential.NONE;

    private final CrowdAddOnUserProvisioningService crowdAddOnUserProvisioningService;
    private final ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService;
    private static final Logger log = LoggerFactory.getLogger(CrowdAddOnUserService.class);
    private final ConnectCrowdService connectCrowdService;
    private final HostProperties hostProperties;

    @Autowired
    public CrowdAddOnUserService(CrowdAddOnUserProvisioningService crowdAddOnUserProvisioningService,
            ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService, ConnectCrowdService connectCrowdService,
            HostProperties hostProperties)
    {
        this.connectCrowdService = connectCrowdService;
        this.hostProperties = hostProperties;
        this.crowdAddOnUserProvisioningService = checkNotNull(crowdAddOnUserProvisioningService);
        this.connectAddOnUserGroupProvisioningService = checkNotNull(connectAddOnUserGroupProvisioningService);
    }

    @Nonnull
    @Override
    public String getOrCreateAddOnUserName(@Nonnull String addOnKey, @Nonnull String addOnDisplayName) throws ConnectAddonInitException
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
            throw new ConnectAddonInitException(e);
        }
    }

    @Override
    public void disableAddOnUser(@Nonnull String addOnKey) throws ConnectAddonDisableException
    {
        connectCrowdService.disableUser(usernameForAddon(addOnKey));
    }

    @Override
    public boolean isActive(@Nonnull String username)
    {
        return connectCrowdService.isUserActive(username);
    }

    @Nullable
    @Override
    public String provisionAddOnUserWithScopes(@Nonnull com.atlassian.plugin.connect.modules.beans.ConnectAddonBean addon, @Nonnull Set<ScopeName> previousScopes, @Nonnull Set<ScopeName> newScopes) throws ConnectAddonInitException
    {
        if (!addOnRequiresUser(addon))
        {
            return null;
        }

        String username = getOrCreateAddOnUserName(checkNotNull(addon.getKey()), checkNotNull(addon.getName()));
        crowdAddOnUserProvisioningService.provisionAddonUserForScopes(username, previousScopes, newScopes);
        return username;
    }

    private String createOrEnableAddOnUser(String username, String addOnDisplayName)
            throws ApplicationNotFoundException, OperationFailedException, ApplicationPermissionException, UserNotFoundException, GroupNotFoundException, InvalidAuthenticationException
    {
        connectAddOnUserGroupProvisioningService.ensureGroupExists(Constants.ADDON_USER_GROUP_KEY);
        UserCreationResult userCreationResult = connectCrowdService.createOrEnableUser(username, addOnDisplayName, Constants.ADDON_USER_EMAIL_ADDRESS, PREVENT_LOGIN, buildConnectAddOnUserAttribute(hostProperties.getKey()));
        User user = userCreationResult.getUser();
        if (!userCreationResult.isNewlyCreated())
        {
            connectCrowdService.invalidateSessions(user.getName());
        }

        connectAddOnUserGroupProvisioningService.ensureUserIsInGroup(user.getName(), Constants.ADDON_USER_GROUP_KEY);
        if (userCreationResult.isNewlyCreated())
        {
            addNewUserToRequiredGroups(user);
        }

        return user.getName();
    }

    private void addNewUserToRequiredGroups(User user)
            throws ApplicationNotFoundException, UserNotFoundException, ApplicationPermissionException, OperationFailedException, InvalidAuthenticationException
    {
        String username = user.getName();
        for (String group : crowdAddOnUserProvisioningService.getDefaultProductGroupsAlwaysExpected())
        {
            try
            {
                connectAddOnUserGroupProvisioningService.ensureUserIsInGroup(username, group);
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
        for (String group : crowdAddOnUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected())
        {
            try
            {
                connectAddOnUserGroupProvisioningService.ensureUserIsInGroup(username, group);
                numPossibleDefaultGroupsAddedTo++;
            }
            catch (GroupNotFoundException e)
            {
                errorMessage += String.format("%s, ", group);
            }

        }
        if (numPossibleDefaultGroupsAddedTo == 0 && crowdAddOnUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected().size() > 0)
        {
            log.error(errorMessage + "because none of those groups exist!" +
                    "We expect at least one of these groups to exist - exactly which one should exist depends on the version of the instance." +
                    "The user needs to be a member of one of these groups for basic access, otherwise the add-on will not function correctly." +
                    "Please check with an instance administrator that at least one of these groups exists and that it is not read-only.");
        }
    }
}

