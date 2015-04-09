package com.atlassian.plugin.connect.plugin.upgrade;

import java.util.Collection;
import java.util.Collections;

import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.crowd.service.factory.CrowdClientFactory;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.plugin.util.FeatureManager;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;

import static com.atlassian.crowd.search.EntityDescriptor.group;
import static com.atlassian.crowd.search.EntityDescriptor.user;
import static com.atlassian.crowd.search.builder.QueryBuilder.queryFor;
import static com.atlassian.crowd.search.query.entity.EntityQuery.ALL_RESULTS;
import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.Constants.ADDON_USER_GROUP_KEY;
import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.getClientProperties;
import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.validAddOnEmailAddress;
import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.validAddOnUsername;

/**
 * A {@link com.atlassian.sal.api.upgrade.PluginUpgradeTask} that will iterate over all Connect AddOn Users and add a new attribute to them
 * <em>synch.APPLICATION_NAME.atlassian-connect-user</em>
 */
public class ConnectAddOnUserAppSpecificAttributeUpgradeTask implements PluginUpgradeTask
{
    private ApplicationService applicationService;
    private ConnectAddOnUserGroupProvisioningService addOnUserGroupProvisioningService;
    private CrowdClientFactory crowdClientFactory;
    private FeatureManager featureManager;

    public ConnectAddOnUserAppSpecificAttributeUpgradeTask(
            ApplicationService applicationService,
            ConnectAddOnUserGroupProvisioningService addOnUserGroupProvisioningService,
            CrowdClientFactory crowdClientFactory, FeatureManager featureManager)
    {
        this.applicationService = applicationService;
        this.addOnUserGroupProvisioningService = addOnUserGroupProvisioningService;
        this.crowdClientFactory = crowdClientFactory;
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
        return "Upgrade task to iterate over all Connect AddOn Users and add a new attribute to them: \"synch.APPLICATION_NAME.atlassian-connect-user\"";
    }

    @Override
    public Collection<Message> doUpgrade() throws Exception
    {
        MembershipQuery<User> membershipQuery = queryFor(User.class, user()).childrenOf(group()).withName(ADDON_USER_GROUP_KEY).returningAtMost(ALL_RESULTS);
        Application application = addOnUserGroupProvisioningService.getCrowdApplication();
        Collection<User> connectAddonUsers = applicationService.searchDirectGroupRelationships(application, membershipQuery);
        for (User user : connectAddonUsers)
        {
            if (!validAddOnUsername(user))
            {
                throw new Exception(String.format("Failed to complete Upgrade Task. User had an invalid username: \"%s\"", user.getName()));
            }
            if (!validAddOnEmailAddress(user))
            {
                throw new Exception(String.format("Failed to complete Upgrade Task. User had an invalid email: \"%s\"", user.getName()));
            }

            applicationService.storeUserAttributes(application, user.getName(), null);
            if (featureManager.isOnDemand())
            {
                // Sets the connect attribute on the Remote Crowd Server if running in OD
                // This is currently required due to the fact that the DbCachingRemoteDirectory implementation used by JIRA and Confluence doesn't currently
                // write attributes back to the Crowd Server. https://ecosystem.atlassian.net/browse/EMBCWD-975 has been raised to look at re-implementing this feature!
                CrowdClient crowdClient = crowdClientFactory.newInstance(getClientProperties());
                crowdClient.storeUserAttributes(user.getName(), null);
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
