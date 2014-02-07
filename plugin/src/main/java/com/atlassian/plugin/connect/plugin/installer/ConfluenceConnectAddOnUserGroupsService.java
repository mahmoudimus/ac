package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

@SuppressWarnings("unused")
@ConfluenceComponent
public class ConfluenceConnectAddOnUserGroupsService implements ConnectAddOnUserGroupsService
{
    @Override
    public Set<String> getDefaultProductGroups()
    {
        // As reported by Sam Day, without the "confluence-users" group the add-on user can't
        // even get the page summary of a page that is open to anonymous access.
        return ImmutableSet.of("confluence-users");
    }
}
