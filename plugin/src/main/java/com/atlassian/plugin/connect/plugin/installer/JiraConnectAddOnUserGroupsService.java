package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

@SuppressWarnings("unused")
@JiraComponent
public class JiraConnectAddOnUserGroupsService implements ConnectAddOnUserGroupsService
{
    private static final ImmutableSet<String> GROUPS = ImmutableSet.of("jira-users");

    @Override
    public Set<String> getDefaultProductGroups()
    {
        // jira-hipchat-discussions revealed that users can't modify issues if not in this group
        return GROUPS;
    }
}
