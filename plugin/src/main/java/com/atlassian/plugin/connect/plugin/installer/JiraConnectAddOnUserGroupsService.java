package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

@SuppressWarnings("unused")
@JiraComponent
public class JiraConnectAddOnUserGroupsService implements ConnectAddOnUserGroupsService
{
    @Override
    public Set<String> getDefaultProductGroups()
    {
        return ImmutableSet.of();
    }
}
