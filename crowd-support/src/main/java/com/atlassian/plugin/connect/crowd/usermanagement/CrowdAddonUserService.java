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
import com.atlassian.plugin.connect.crowd.spi.CrowdAddonUserProvisioningService;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.HostProperties;
import com.atlassian.plugin.connect.spi.auth.user.ConnectUserService;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonDisableException;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonInitException;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddonUserUtil.Constants;
import static com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddonUserUtil.buildConnectAddonUserAttribute;
import static com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddonUserUtil.usernameForAddon;
import static com.google.common.base.Preconditions.checkNotNull;

@ExportAsDevService
@ConfluenceComponent
@JiraComponent
public class CrowdAddonUserService implements ConnectUserService {
    public static final PasswordCredential PREVENT_LOGIN = PasswordCredential.NONE;

    private final CrowdAddonUserProvisioningService crowdAddonUserProvisioningService;
    private final ConnectAddonUserGroupProvisioningService connectAddonUserGroupProvisioningService;
    private static final Logger log = LoggerFactory.getLogger(CrowdAddonUserService.class);
    private final ConnectCrowdService connectCrowdService;
    private final HostProperties hostProperties;

    @Autowired
    public CrowdAddonUserService(CrowdAddonUserProvisioningService crowdAddonUserProvisioningService,
                                 ConnectAddonUserGroupProvisioningService connectAddonUserGroupProvisioningService, ConnectCrowdService connectCrowdService,
                                 HostProperties hostProperties) {
        this.connectCrowdService = connectCrowdService;
        this.hostProperties = hostProperties;
        this.crowdAddonUserProvisioningService = checkNotNull(crowdAddonUserProvisioningService);
        this.connectAddonUserGroupProvisioningService = checkNotNull(connectAddonUserGroupProvisioningService);
    }

    @Nonnull
    @Override
    public String getOrCreateAddonUserName(@Nonnull String addonKey, @Nonnull String addonDisplayName) throws ConnectAddonInitException {
        try {
            return createOrEnableAddonUser(ConnectAddonUserUtil.usernameForAddon(checkNotNull(addonKey)), checkNotNull(addonDisplayName));
        } catch (ApplicationPermissionException
                | UserNotFoundException
                | GroupNotFoundException
                | ApplicationNotFoundException
                | OperationFailedException
                | InvalidAuthenticationException e) {
            throw new ConnectAddonInitException(e);
        }
    }

    @Override
    public void disableAddonUser(@Nonnull String addonKey) throws ConnectAddonDisableException {
        connectCrowdService.disableUser(usernameForAddon(addonKey));
    }

    @Override
    public boolean isActive(@Nonnull String username) {
        return connectCrowdService.isUserActive(username);
    }

    @Nonnull
    @Override
    public String provisionAddonUserWithScopes(@Nonnull ConnectAddonBean addon, @Nonnull Set<ScopeName> previousScopes, @Nonnull Set<ScopeName> newScopes) throws ConnectAddonInitException {
        String username = getOrCreateAddonUserName(checkNotNull(addon.getKey()), checkNotNull(addon.getName()));
        crowdAddonUserProvisioningService.provisionAddonUserForScopes(username, previousScopes, newScopes);
        return username;
    }

    private String createOrEnableAddonUser(String username, String addonDisplayName)
            throws ApplicationNotFoundException, OperationFailedException, ApplicationPermissionException, UserNotFoundException, GroupNotFoundException, InvalidAuthenticationException {
        connectAddonUserGroupProvisioningService.ensureGroupExists(Constants.ADDON_USER_GROUP_KEY);
        UserCreationResult userCreationResult = connectCrowdService.createOrEnableUser(username, addonDisplayName, Constants.ADDON_USER_EMAIL_ADDRESS, PREVENT_LOGIN, buildConnectAddonUserAttribute(hostProperties.getKey()));
        User user = userCreationResult.getUser();
        if (!userCreationResult.isNewlyCreated()) {
            connectCrowdService.invalidateSessions(user.getName());
        }

        connectAddonUserGroupProvisioningService.ensureUserIsInGroup(user.getName(), Constants.ADDON_USER_GROUP_KEY);
        if (userCreationResult.isNewlyCreated()) {
            addNewUserToRequiredGroups(user);
        }

        return user.getName();
    }

    private void addNewUserToRequiredGroups(User user)
            throws ApplicationNotFoundException, UserNotFoundException, ApplicationPermissionException, OperationFailedException, InvalidAuthenticationException {
        String username = user.getName();
        for (String group : crowdAddonUserProvisioningService.getDefaultProductGroupsAlwaysExpected()) {
            try {
                connectAddonUserGroupProvisioningService.ensureUserIsInGroup(username, group);
            } catch (GroupNotFoundException e) {
                // carry on if the group does not exist so that an admin deleting a group will not kill all add-on installations
                log.error(String.format("Could not make user '%s' a member of group '%s' because that group does not exist! " +
                        "The user needs to be a member of this group, otherwise the add-on will not function correctly. " +
                        "Please check with an instance administrator that this group exists and that it is not read-only.", username, group));
                // TODO ACDEV-938: propagate this error
            }
        }

        int numPossibleDefaultGroupsAddedTo = 0;
        String errorMessage = String.format("Could not make user '%s' a member of one of groups ", username);
        for (String group : crowdAddonUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected()) {
            try {
                connectAddonUserGroupProvisioningService.ensureUserIsInGroup(username, group);
                numPossibleDefaultGroupsAddedTo++;
            } catch (GroupNotFoundException e) {
                errorMessage += String.format("%s, ", group);
            }

        }
        if (numPossibleDefaultGroupsAddedTo == 0 && crowdAddonUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected().size() > 0) {
            log.error(errorMessage + "because none of those groups exist!" +
                    "We expect at least one of these groups to exist - exactly which one should exist depends on the version of the instance." +
                    "The user needs to be a member of one of these groups for basic access, otherwise the add-on will not function correctly." +
                    "Please check with an instance administrator that at least one of these groups exists and that it is not read-only.");
        }
    }
}

