package com.atlassian.plugin.connect.plugin.upgrade;

import java.util.Collection;
import java.util.Collections;

import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.plugin.usermanagement.CrowdClientFacade;
import com.atlassian.plugin.connect.plugin.util.FeatureManager;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.buildAttributeConnectAddOnAttributeName;
import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.buildConnectAddOnUserAttribute;
import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.validAddOnEmailAddress;
import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.validAddOnUsername;

/**
 * A {@link com.atlassian.sal.api.upgrade.PluginUpgradeTask} that will iterate over all Connect AddOn Users and add a new attribute to them
 * <em>synch.APPLICATION_NAME.atlassian-connect-user</em>
 */
@ExportAsService
@ConfluenceComponent
@JiraComponent
public class ConnectAddOnUserAppSpecificAttributeUpgradeTask implements PluginUpgradeTask
{
    public static final String OLD_ATTRIBUTE_APPLICATION_NAME = "crowd-embedded";
    private ApplicationService applicationService;
    private final ConnectAddOnUsers connectAddOnUsers;
    private ConnectAddOnUserGroupProvisioningService addOnUserGroupProvisioningService;
    private final CrowdClientFacade crowdClientFacade;
    private FeatureManager featureManager;

    @Autowired
    public ConnectAddOnUserAppSpecificAttributeUpgradeTask(
            ApplicationService applicationService,
            ConnectAddOnUsers connectAddOnUsers,
            ConnectAddOnUserGroupProvisioningService addOnUserGroupProvisioningService,
            CrowdClientFacade crowdClientFacade,
            FeatureManager featureManager)
    {
        this.applicationService = applicationService;
        this.connectAddOnUsers = connectAddOnUsers;
        this.addOnUserGroupProvisioningService = addOnUserGroupProvisioningService;
        this.crowdClientFacade = crowdClientFacade;
        this.featureManager = featureManager;
    }

    @Override
    public int getBuildNumber()
    {
        return 2;
    }

    @Override
    public String getShortDescription()
    {
        return "Upgrade task to iterate over all Connect AddOn Users, delete their old attribute (\"synch.crowd-embedded.atlassian-connect-user\"), and add a new attribute to them: \"synch.HOST_APPLICATION_NAME.atlassian-connect-user\"";
    }

    @Override
    public Collection<Message> doUpgrade() throws Exception
    {
        Application application = addOnUserGroupProvisioningService.getCrowdApplication();
        for (User user : connectAddOnUsers.getAddonUsersToUpgradeForHostProduct())
        {
            if (!validAddOnUsername(user))
            {
                throw new Exception(String.format("Failed to complete Upgrade Task. User had an invalid username: \"%s\"", user.getName()));
            }
            if (!validAddOnEmailAddress(user))
            {
                throw new Exception(String.format("Failed to complete Upgrade Task. User had an invalid email: \"%s\"", user.getName()));
            }

            applicationService.storeUserAttributes(application, user.getName(), buildConnectAddOnUserAttribute(crowdClientFacade.getClientApplicationName()));
            if (featureManager.isOnDemand())
            {
                // Sets the connect attribute on the Remote Crowd Server if running in OD
                // This is currently required due to the fact that the DbCachingRemoteDirectory implementation used by JIRA and Confluence doesn't currently
                // write attributes back to the Crowd Server. https://ecosystem.atlassian.net/browse/EMBCWD-975 has been raised to look at re-implementing this feature!
                CrowdClient crowdClient = crowdClientFacade.getCrowdClient();
                crowdClient.storeUserAttributes(user.getName(), buildConnectAddOnUserAttribute(crowdClientFacade.getClientApplicationName()));
            }
        }

        for (User user : connectAddOnUsers.getAddonUsersToClean())
        {
            applicationService.removeUserAttributes(application, user.getName(), buildAttributeConnectAddOnAttributeName(OLD_ATTRIBUTE_APPLICATION_NAME));
            if (featureManager.isOnDemand())
            {
                CrowdClient crowdClient = crowdClientFacade.getCrowdClient();
                crowdClient.removeUserAttributes(user.getName(), buildAttributeConnectAddOnAttributeName(OLD_ATTRIBUTE_APPLICATION_NAME));
            }
        }

        return Collections.emptyList();
    }

    @Override
    public String getPluginKey()
    {
        return ConnectPluginInfo.getPluginKey();
    }
}
