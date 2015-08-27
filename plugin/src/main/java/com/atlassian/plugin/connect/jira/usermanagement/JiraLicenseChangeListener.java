package com.atlassian.plugin.connect.jira.usermanagement;

import java.util.HashSet;
import java.util.Set;

import com.atlassian.application.api.ApplicationKey;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.application.ApplicationAuthorizationService;
import com.atlassian.jira.application.ApplicationRoleManager;
import com.atlassian.jira.license.LicenseChangedEvent;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddOnUsers;

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
        boolean ignoreEvent = false;
        log.info("Received a LicenseChangedEvent");
        if (!applicationAuthorizationService.rolesEnabled())
        {
            log.info("License roles are not enabled");
            ignoreEvent = true;
        }
        if (event.getPreviousLicenseDetails().isEmpty())
        {
            log.info("No previous license details");
            ignoreEvent = true;
        }
        if (event.getNewLicenseDetails().isEmpty())
        {
            log.info("No new license details");
            ignoreEvent = true;
        }
        if (ignoreEvent)
        {
            log.info("Ignoring LicenseChangedEvent");
            return;
        }
        else
        {
            log.info("Handling LicenseChangedEvent");
        }

        Set<ApplicationKey> oldKeys = event.getPreviousLicenseDetails().get().getLicensedApplications().getKeys();
        Set<ApplicationKey> newKeys = new HashSet<>(event.getNewLicenseDetails().get().getLicensedApplications().getKeys());
        newKeys.removeAll(oldKeys);
        Set<String> newGroups = new HashSet<>();
        StringBuilder newAppsMessage = new StringBuilder("Detected new applications: ");
        for (ApplicationKey key : newKeys)
        {
            newAppsMessage.append(key).append(" ");
            for (Group group : applicationRoleManager.getDefaultGroups(key))
            {
                newGroups.add(group.getName());
            }
        }
        log.info(newAppsMessage.toString());

        for (User addonUser : connectAddOnUsers.getAddonUsers())
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
