package com.atlassian.plugin.connect.crowd.usermanagement;

import java.util.Set;
import java.util.stream.Collectors;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import com.google.common.base.Predicate;

import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.crowd.search.EntityDescriptor.group;
import static com.atlassian.crowd.search.EntityDescriptor.user;
import static com.atlassian.crowd.search.builder.QueryBuilder.queryFor;
import static com.atlassian.crowd.search.query.entity.EntityQuery.ALL_RESULTS;
import static com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddonUserUtil.Constants.ADDON_USERNAME_PREFIX;
import static com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddonUserUtil.Constants.ADDON_USER_GROUP_KEY;
import static com.google.common.collect.Iterables.filter;

@JiraComponent
public class ConnectAddonUsers
{
    private final ConnectAddonAccessor addonAccessor;
    private final ApplicationService applicationService;
    private final CrowdApplicationProvider crowdApplicationProvider;
    private final MembershipQuery<User> membershipQuery;

    @Autowired
    public ConnectAddonUsers(ConnectAddonAccessor addonAccessor, ApplicationService applicationService, CrowdApplicationProvider crowdApplicationProvider)
    {
        this.addonAccessor = addonAccessor;
        this.applicationService = applicationService;
        this.crowdApplicationProvider = crowdApplicationProvider;
        membershipQuery = queryFor(User.class, user()).childrenOf(group()).withName(ADDON_USER_GROUP_KEY).returningAtMost(ALL_RESULTS);
    }

    public Iterable<User> getAddonUsers()
    {
        try
        {
            return filter(applicationService.searchDirectGroupRelationships(crowdApplicationProvider.getCrowdApplication(), membershipQuery),
                    isHostProductAddonUserKey());
        }
        catch (ApplicationNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Predicate<User> isHostProductAddonUserKey()
    {
        final Set<String> allAddonUserKeys = getAddonUserKeys();
        return user -> allAddonUserKeys.contains(user.getName());
    }

    private Set<String> getAddonUserKeys()
    {
        return addonAccessor.getAllAddonKeys().stream()
            .map(addonKey -> ADDON_USERNAME_PREFIX + addonKey)
            .collect(Collectors.toSet());
    }
}
