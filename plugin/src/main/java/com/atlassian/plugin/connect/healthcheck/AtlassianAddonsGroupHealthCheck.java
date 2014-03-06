package com.atlassian.plugin.connect.healthcheck;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.healthcheck.core.DefaultHealthStatus;
import com.atlassian.healthcheck.core.HealthCheck;
import com.atlassian.healthcheck.core.HealthStatus;
import com.atlassian.healthcheck.core.HealthStatusExtended;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.Constants;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

public class AtlassianAddonsGroupHealthCheck implements HealthCheck
{
    private final ApplicationManager applicationManager;
    private final ApplicationService applicationService;
    private final ConnectAddOnUserGroupProvisioningService groupProvisioningService;

    public AtlassianAddonsGroupHealthCheck(ApplicationManager applicationManager, ApplicationService applicationService,
            ConnectAddOnUserGroupProvisioningService groupProvisioningService)
    {
        this.applicationManager = applicationManager;
        this.applicationService = applicationService;
        this.groupProvisioningService = groupProvisioningService;
    }

    @Override
    public HealthStatus check()
    {
        long healthCheckTime = System.currentTimeMillis();
        String documentationUrl = ""; // lol, as if.
        try
        {
            Application application = applicationManager.findByName(groupProvisioningService.getCrowdApplicationName());

            MembershipQuery<User> query = QueryBuilder
                .queryFor(User.class, EntityDescriptor.user())
                .childrenOf(EntityDescriptor.group())
                .withName(Constants.ADDON_USER_GROUP_KEY)
                .startingAt(0)
                .returningAtMost(EntityQuery.ALL_RESULTS);

            List<User> users = applicationService.searchDirectGroupRelationships(application, query);

            Set<User> usersWithIncorrectEmails = Sets.newHashSet();
            Set<User> usersWithIncorrectPrefix = Sets.newHashSet();
            for (User user : users)
            {
                if (!Constants.ADDON_USER_EMAIL_ADDRESS.equals(user.getEmailAddress()))
                {
                    usersWithIncorrectEmails.add(user);
                }
                String name = user.getName();
                if (name == null || !name.startsWith(Constants.ADDON_USER_KEY_PREFIX))
                {
                    usersWithIncorrectPrefix.add(user);
                }
            }

            boolean isHealthy = usersWithIncorrectEmails.isEmpty() && usersWithIncorrectPrefix.isEmpty();

            String reason = "";

            if (!isHealthy)
            {
                reason = "Add-on group has invalid membership: ";
                if (!usersWithIncorrectEmails.isEmpty())
                {
                    reason += usersWithIncorrectEmails.size() + " members have unexpected email values. ";
                }
                if (!usersWithIncorrectPrefix.isEmpty())
                {
                    reason += usersWithIncorrectPrefix.size() + " members have unexpected username values. ";
                }
                reason += "This may indicate a customer license workaround.";
            }

            return new DefaultHealthStatus(isHealthy, reason, healthCheckTime, com.atlassian.healthcheck.core.Application.Plugin,
                    HealthStatusExtended.Severity.CRITICAL, documentationUrl);
        }
        catch (ApplicationNotFoundException e)
        {
            return new DefaultHealthStatus(false, "Could not find application " + e.getApplicationName(),
                    healthCheckTime, com.atlassian.healthcheck.core.Application.Plugin,
                    HealthStatusExtended.Severity.CRITICAL, documentationUrl);
        }
    }
}
