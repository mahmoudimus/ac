package com.atlassian.plugin.connect.plugin.usermanagement.upgrade;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;

import com.google.common.collect.ImmutableMap;

import static com.atlassian.crowd.search.builder.QueryBuilder.queryFor;
import static com.atlassian.crowd.search.query.entity.EntityQuery.ALL_RESULTS;
import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.Constants.ADDON_USER_GROUP_KEY;

/**
 * A {@link com.atlassian.sal.api.upgrade.PluginUpgradeTask} that will iterate over all Connect AddOn Users and add a new attribute to them
 * <em>synch.APPLICATION_NAME.atlassian-connect-user</em>
 */
public class ConnectAddOnUserAttributeUpgradeTask implements PluginUpgradeTask
{
    private final ApplicationService applicationService;
    private final ApplicationManager applicationManager;
    private final ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService;

    public ConnectAddOnUserAttributeUpgradeTask(ApplicationService applicationService, ApplicationManager applicationManager, ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService)
    {
        this.applicationService = applicationService;
        this.applicationManager = applicationManager;
        this.connectAddOnUserGroupProvisioningService = connectAddOnUserGroupProvisioningService;
    }

    @Override
    public int getBuildNumber()
    {
        return 1;
    }

    @Override
    public String getShortDescription()
    {
        return "Upgrade task to iterate over all Connect AddOn Users and add a new attribute to them synch.APPLICATION_NAME.atlassian-connect-user";
    }

    @Override
    public Collection<Message> doUpgrade() throws Exception
    {
        MembershipQuery<String> membershipQuery = queryFor(String.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(ADDON_USER_GROUP_KEY).returningAtMost(ALL_RESULTS);

        List<String> connectAddonUsers = applicationService.searchDirectGroupRelationships(getApplication(), membershipQuery);

        for (String user : connectAddonUsers)
        {
            // Set connect attributes on user
            applicationService.storeUserAttributes(getApplication(), user, ImmutableMap.of("synch." + getApplication().getName() + ".atlassian-connect-user", Collections.singleton("true")));
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
