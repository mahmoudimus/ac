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
                .withName("atlassian-addons")
                .startingAt(0)
                .returningAtMost(EntityQuery.ALL_RESULTS);

            List<User> users = applicationService.searchDirectGroupRelationships(application, query);

            Set<User> usersWithIncorrectEmails = Sets.newHashSet();
            for (User user : users)
            {
                if (!"noreply@mailer.atlassian.com".equals(user.getEmailAddress()))
                {
                    usersWithIncorrectEmails.add(user);
                }
            }

            boolean isHealthy = usersWithIncorrectEmails.isEmpty();
            String reason = isHealthy ? "" : "Add-on group has users with incorrect email addresses. This may indicate a customer license workaround.";
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
