package com.atlassian.plugin.connect.jira.auth;

import java.util.HashSet;
import java.util.Set;

import com.atlassian.application.api.ApplicationKey;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.application.ApplicationAuthorizationService;
import com.atlassian.jira.application.ApplicationRoleManager;
import com.atlassian.jira.license.LicenseChangedEvent;
import com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddonUserGroupProvisioningService;
import com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddonUsers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.collect.Sets.difference;

public class JiraLicenseChangeListener
{
    private static final Logger log = LoggerFactory.getLogger(JiraLicenseChangeListener.class);
    private final ApplicationRoleManager applicationRoleManager;
    private final ConnectAddonUsers connectAddonUsers;
    private final ConnectAddonUserGroupProvisioningService connectAddonUserGroupProvisioningService;
    private final ApplicationAuthorizationService applicationAuthorizationService;

    @Autowired
    public JiraLicenseChangeListener(ApplicationRoleManager applicationRoleManager, ConnectAddonUsers connectAddonUsers, ConnectAddonUserGroupProvisioningService connectAddonUserGroupProvisioningService, ApplicationAuthorizationService applicationAuthorizationService)
    {
        this.applicationRoleManager = applicationRoleManager;
        this.connectAddonUsers = connectAddonUsers;
        this.connectAddonUserGroupProvisioningService = connectAddonUserGroupProvisioningService;
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
        Set<ApplicationKey> newKeys = event.getNewLicenseDetails().get().getLicensedApplications().getKeys();
        addApplicationUsersToDefaultApplicationGroups(difference(newKeys, oldKeys));
    }

    private void addApplicationUsersToDefaultApplicationGroups(Set<ApplicationKey> keys)
    {
        Set<String> newGroups = new HashSet<>();
        StringBuilder newAppsMessage = new StringBuilder("Found the following applications and groups: ");
        for (ApplicationKey key : keys)
        {
            newAppsMessage.append(key).append(": [ ");
            for (Group group : applicationRoleManager.getDefaultGroups(key))
            {
                newAppsMessage.append(group.getName()).append(" ");
                newGroups.add(group.getName());
            }
            newAppsMessage.append("] ");
        }
        log.info(newAppsMessage.toString());

        for (User addonUser : connectAddonUsers.getAddonUsers())
        {
            try
            {
                connectAddonUserGroupProvisioningService.ensureUserIsInGroups(addonUser.getName(), newGroups);
            }
            catch (Exception e)
            {
                log.error("Error adding addon user {} to new application default groups", addonUser.getName(), e);
            }
        }
    }
}
