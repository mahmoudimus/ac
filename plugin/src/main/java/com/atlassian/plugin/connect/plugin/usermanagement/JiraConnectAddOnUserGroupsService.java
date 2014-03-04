package com.atlassian.plugin.connect.plugin.usermanagement;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.any;

@SuppressWarnings("unused")
@JiraComponent
public class JiraConnectAddOnUserGroupsService implements ConnectAddOnUserGroupsService
{
    private static final ImmutableSet<String> GROUPS = ImmutableSet.of("jira-users");
    private static final int ADMIN_PERMISSION = Permissions.Permission.ADMINISTER.getId();

    private final GlobalPermissionManager jiraPermissionManager;

    @Inject
    public JiraConnectAddOnUserGroupsService(GlobalPermissionManager jiraPermissionManager)
    {
        this.jiraPermissionManager = checkNotNull(jiraPermissionManager);
    }

    @Override
    public Set<String> getDefaultProductGroups()
    {
        // jira-hipchat-discussions revealed that users can't modify issues if not in this group
        return GROUPS;
    }

    @Override
    public void ensureIsAdmin(String groupKey)
    {
        if (!isAdmin(groupKey))
        {
            jiraPermissionManager.addPermission(ADMIN_PERMISSION, groupKey);
        }
    }

    @Override
    public boolean isAdmin(final String groupKey)
    {
        checkNotNull(groupKey);
        return any(jiraPermissionManager.getGroupsWithPermission(ADMIN_PERMISSION), new Predicate<Group>()
        {
            @Override
            public boolean apply(@Nullable Group group)
            {
                return null != group && groupKey.equals(group.getName());
            }
        });
    }
}
