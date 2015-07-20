package com.atlassian.plugin.connect.crowd.healthcheck;

import java.util.Collection;
import java.util.Set;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
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
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil.Constants;
import com.atlassian.plugin.connect.crowd.usermanagement.CrowdApplicationProvider;

import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil.validAddOnEmailAddress;
import static com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil.validAddOnUsername;

public class AtlassianAddonsGroupHealthCheck implements HealthCheck
{
    // Used until we can upgrade to health check 2.0.7
    private static final String CHECK_NAME = "com.atlassian.plugins.atlassian-connect-plugin:addonsGroupHealthCheck";
    private static final String CHECK_DESCRIPTION = "This was provided by plugin 'com.atlassian.plugins.atlassian-connect-plugin:addonsGroupHealthCheck' via class 'com.atlassian.plugin.connect.plugin.AtlassianAddonsGroupHealthCheck'";

    private static final Logger log = LoggerFactory.getLogger(AtlassianAddonsGroupHealthCheck.class);

    private final ApplicationService applicationService;
    private final CrowdApplicationProvider crowdApplicationProvider;

    public AtlassianAddonsGroupHealthCheck(ApplicationService applicationService,
            CrowdApplicationProvider crowdApplicationProvider)
    {
        this.applicationService = applicationService;
        this.crowdApplicationProvider = crowdApplicationProvider;
    }

    @Override
    public HealthStatus check()
    {
        long healthCheckTime = System.currentTimeMillis();

        try
        {
            Collection<User> users = getAddonUsers();

            Set<User> usersWithIncorrectEmails = Sets.newHashSet();
            Set<User> usersWithIncorrectPrefix = Sets.newHashSet();
            Set<User> usersIncorrectlyActive = Sets.newHashSet();

            for (User user : users)
            {
                if (!validAddOnEmailAddress(user))
                {
                    log.warn("Add-on user '" + user.getName() + "' has incorrect email '" + user.getEmailAddress() + "'");
                    usersWithIncorrectEmails.add(user);
                }

                if (!validAddOnUsername(user))
                {
                    log.warn("Add-on user '" + user.getName() + "' has incorrect prefix");
                    usersWithIncorrectPrefix.add(user);
                }
            }

            boolean isHealthy = usersWithIncorrectEmails.isEmpty() && usersWithIncorrectPrefix.isEmpty() && usersIncorrectlyActive.isEmpty();

            StringBuilder reason = new StringBuilder();

            if (!isHealthy)
            {
                reason.append("Add-on group has invalid membership: ");

                if (!usersWithIncorrectEmails.isEmpty())
                {
                    reason.append(failurePrefix(usersWithIncorrectEmails.size())).append(" unexpected email values. ");
                }
                if (!usersWithIncorrectPrefix.isEmpty())
                {
                    reason.append(failurePrefix(usersWithIncorrectPrefix.size())).append(" unexpected username values. ");
                }
                if (!usersIncorrectlyActive.isEmpty())
                {
                    reason.append(failurePrefix(usersIncorrectlyActive.size())).append(" no applink association. ");
                }

                reason.append("This may indicate a customer license workaround.");
            }

            return new DefaultHealthStatus(CHECK_NAME, CHECK_DESCRIPTION, com.atlassian.healthcheck.core.Application.Plugin,
                    isHealthy, reason.toString(), healthCheckTime);
        }
        catch (ApplicationNotFoundException e)
        {
            return new DefaultHealthStatus(CHECK_NAME, CHECK_DESCRIPTION, com.atlassian.healthcheck.core.Application.Plugin,
                    false, "Could not find application " + e.getApplicationName(), healthCheckTime);
        }
    }

    private String failurePrefix(final int size)
    {
        return size + (size == 1 ? " member has" : " members have");
    }

    protected Collection<User> getAddonUsers() throws ApplicationNotFoundException
    {
        Application application = crowdApplicationProvider.getCrowdApplication();

        MembershipQuery<User> query = QueryBuilder
                .queryFor(User.class, EntityDescriptor.user())
                .childrenOf(EntityDescriptor.group())
                .withName(Constants.ADDON_USER_GROUP_KEY)
                .startingAt(0)
                .returningAtMost(EntityQuery.ALL_RESULTS);

        return applicationService.searchDirectGroupRelationships(application, query);
    }
}
