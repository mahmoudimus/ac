package com.atlassian.plugin.connect.jira.usermanagement;

import java.util.HashSet;
import java.util.Set;

import com.atlassian.application.api.ApplicationKey;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.application.ApplicationAuthorizationService;
import com.atlassian.jira.application.ApplicationRoleManager;
import com.atlassian.jira.license.LicenseChangedEvent;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddOnUsers;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JiraLicenseChangeListener
{
    private static final Logger log = LoggerFactory.getLogger(JiraLicenseChangeListener.class);
    private final ApplicationRoleManager applicationRoleManager;
    private final ConnectAddOnUsers connectAddOnUsers;
    private final ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService;
    private final ApplicationAuthorizationService applicationAuthorizationService;

    public JiraLicenseChangeListener(ApplicationRoleManager applicationRoleManager, ConnectAddOnUsers connectAddOnUsers, ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService, ApplicationAuthorizationService applicationAuthorizationService)
    {
        this.applicationRoleManager = applicationRoleManager;
        this.connectAddOnUsers = connectAddOnUsers;
        this.connectAddOnUserGroupProvisioningService = connectAddOnUserGroupProvisioningService;
        this.applicationAuthorizationService = applicationAuthorizationService;
    }

    @EventListener
    public void onLicenseChanged(LicenseChangedEvent event)
    {
        if (!applicationAuthorizationService.rolesEnabled()
                || event.getPreviousLicenseDetails().isEmpty() || event.getNewLicenseDetails().isEmpty())
        {
            // Renaissance is disabled, the host got its license for the first time, or the host has lost its license.
            // There shouldn't be any add-ons that we need to adjust.

            return;
        }

        Set<ApplicationKey> oldKeys = event.getPreviousLicenseDetails().get().getLicensedApplications().getKeys();
        Set<ApplicationKey> newKeys = event.getNewLicenseDetails().get().getLicensedApplications().getKeys();
        newKeys.removeAll(oldKeys);
        Set<String> newGroups = new HashSet<>();
        for (ApplicationKey key : newKeys)
        {
            for (Group group : applicationRoleManager.getDefaultGroups(key))
            {
                newGroups.add(group.getName());
            }
        }

        for(User addonUser : connectAddOnUsers.getAddonUsers())
        {
            try
            {
                connectAddOnUserGroupProvisioningService.ensureUserIsInGroups(addonUser.getName(), newGroups);
            }
            catch (Exception e)
            {
                log.error("Error adding addon user {} to new application default groups", addonUser.getName(), e);
            }
        }
    }
}
