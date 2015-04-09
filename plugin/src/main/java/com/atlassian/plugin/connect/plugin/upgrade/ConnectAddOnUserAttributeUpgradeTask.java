package com.atlassian.plugin.connect.plugin.upgrade;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.crowd.service.factory.CrowdClientFactory;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.plugin.util.FeatureManager;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.crowd.search.builder.QueryBuilder.queryFor;
import static com.atlassian.crowd.search.query.entity.EntityQuery.ALL_RESULTS;
import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.Constants.ADDON_USER_GROUP_KEY;
import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.buildConnectAddOnUserAttribute;
import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.getClientProperties;
import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.validAddOnEmailAddress;
import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.validAddOnUsername;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link com.atlassian.sal.api.upgrade.PluginUpgradeTask} that will iterate over all Connect AddOn Users and add a new attribute to them
 * <em>synch.crowd-embedded.atlassian-connect-user</em>
 * This is superseded by {@link com.atlassian.plugin.connect.plugin.upgrade.ConnectAddOnUserAppSpecificAttributeUpgradeTask},
 * which uses the actual application name (<tt>"jira"</tt> / <tt>"confluence"</tt>) in place of <tt>"crowd-embedded"</tt>
 */
@ExportAsService
@Component
public class ConnectAddOnUserAttributeUpgradeTask implements PluginUpgradeTask
{
    private final ApplicationService applicationService;
    private final ApplicationManager applicationManager;
    private final ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService;
    private final CrowdClientFactory crowdClientFactory;
    private final FeatureManager featureManager;

    @Autowired
    public ConnectAddOnUserAttributeUpgradeTask(
            ApplicationService applicationService,
            ApplicationManager applicationManager,
            ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService,
            CrowdClientFactory crowdClientFactory, FeatureManager featureManager)
    {
        this.featureManager = featureManager;
        this.applicationService = checkNotNull(applicationService);
        this.applicationManager = checkNotNull(applicationManager);
        this.crowdClientFactory = checkNotNull(crowdClientFactory);
        this.connectAddOnUserGroupProvisioningService = checkNotNull(connectAddOnUserGroupProvisioningService);
    }

    @Override
    public int getBuildNumber()
    {
        return 1;
    }

    @Override
    public String getShortDescription()
    {
        return "Upgrade task to iterate over all Connect AddOn Users and add a new attribute to them: \"synch.crowd-embedded.atlassian-connect-user\"";
    }

    @Override
    public Collection<Message> doUpgrade() throws Exception
    {
        MembershipQuery<User> membershipQuery = queryFor(User.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(ADDON_USER_GROUP_KEY).returningAtMost(ALL_RESULTS);

        Application application = getApplication();
        List<User> connectAddonUsers = applicationService.searchDirectGroupRelationships(application, membershipQuery);

        for (User user : connectAddonUsers)
        {
            // Validate the connect-addon user
            if (!validAddOnEmailAddress(user) || !validAddOnUsername(user))
            {
                throw new Exception(String.format("Failed to complete Upgrade Task. User had an invalid username %s or email address %s", user.getName(), user.getEmailAddress()));
            }
            // Set connect attributes on user
            applicationService.storeUserAttributes(application, user.getName(), buildConnectAddOnUserAttribute(application.getName()));

            if (featureManager.isOnDemand())
            {
                // Sets the connect attribute on the Remote Crowd Server if running in OD
                // This is currently required due to the fact that the DbCachingRemoteDirectory implementation used by JIRA and Confluence doesn't currently
                // write attributes back to the Crowd Server. https://ecosystem.atlassian.net/browse/EMBCWD-975 has been raised to look at re-implementing this
                // feature!
                CrowdClient crowdClient = crowdClientFactory.newInstance(getClientProperties());
                crowdClient.storeUserAttributes(user.getName(), buildConnectAddOnUserAttribute(application.getName()));
            }
        }

        return Collections.emptyList();
    }

    @Override
    public String getPluginKey()
    {
        return ConnectPluginInfo.getPluginKey();
    }

    private Application getApplication() throws ApplicationNotFoundException
    {
        return applicationManager.findByName(connectAddOnUserGroupProvisioningService.getCrowdApplicationName());
    }
}
