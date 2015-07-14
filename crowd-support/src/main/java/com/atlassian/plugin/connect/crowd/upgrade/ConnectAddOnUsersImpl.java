package com.atlassian.plugin.connect.crowd.upgrade;

import java.util.HashSet;
import java.util.Set;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserGroupProvisioningService;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.crowd.search.EntityDescriptor.group;
import static com.atlassian.crowd.search.EntityDescriptor.user;
import static com.atlassian.crowd.search.builder.QueryBuilder.queryFor;
import static com.atlassian.crowd.search.query.entity.EntityQuery.ALL_RESULTS;
import static com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil.Constants.ADDON_USERNAME_PREFIX;
import static com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil.Constants.ADDON_USER_GROUP_KEY;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;

@Component
public class ConnectAddOnUsersImpl implements ConnectAddOnUsers
{
    private final ConnectAddonRegistry connectAddOnRegistry;
    private final ApplicationService applicationService;
    private final ConnectAddOnUserGroupProvisioningService userGroupProvisioningService;
    private final MembershipQuery<User> membershipQuery;

    @Autowired
    public ConnectAddOnUsersImpl(ConnectAddonRegistry connectAddOnRegistry, ApplicationService applicationService, ConnectAddOnUserGroupProvisioningService userGroupProvisioningService)
    {
        this.connectAddOnRegistry = connectAddOnRegistry;
        this.applicationService = applicationService;
        this.userGroupProvisioningService = userGroupProvisioningService;
        membershipQuery = queryFor(User.class, user()).childrenOf(group()).withName(ADDON_USER_GROUP_KEY).returningAtMost(ALL_RESULTS);
    }

    @Override
    public Iterable<User> getAddonUsersToUpgradeForHostProduct()
            throws ApplicationNotFoundException
    {
        return filter(applicationService.searchDirectGroupRelationships(userGroupProvisioningService.getCrowdApplication(), membershipQuery),
                isHostProductAddonUserKey());
    }

    @Override
    public Iterable<User> getAddonUsersToClean()
            throws ApplicationNotFoundException
    {
        return applicationService.searchDirectGroupRelationships(userGroupProvisioningService.getCrowdApplication(), membershipQuery);
    }

    private Predicate<User> isHostProductAddonUserKey()
    {
        final Set<String> allAddonUserKeys = getAddonUserKeys();
        return new Predicate<User>()
        {
            @Override
            public boolean apply(User user)
            {
                return allAddonUserKeys.contains(user.getName());
            }
        };
    }

    private HashSet<String> getAddonUserKeys()
    {
        return newHashSet(transform(connectAddOnRegistry.getAllAddonKeys(), new Function<String, String>()
        {
            @Override
            public String apply(String addonKey)
            {
                return ADDON_USERNAME_PREFIX + addonKey;
            }
        }));
    }
}
