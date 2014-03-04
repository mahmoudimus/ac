package com.atlassian.plugin.connect.plugin.usermanagement.jira;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserProvisioningService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.any;

@SuppressWarnings ("unused")
@JiraComponent
@ExportAsDevService
public class JiraAddOnUserProvisioningService implements ConnectAddOnUserProvisioningService
{
    private static final ImmutableSet<String> GROUPS = ImmutableSet.of("jira-users");
    private static final int ADMIN_PERMISSION = Permissions.ADMINISTER;

    private final GlobalPermissionManager jiraPermissionManager;
    private final ProjectService projectService;
    private final UserManager userManager;

    @Inject
    public JiraAddOnUserProvisioningService(GlobalPermissionManager jiraPermissionManager, ProjectService projectService,
            UserManager userManager)
    {
        this.jiraPermissionManager = checkNotNull(jiraPermissionManager);
        this.projectService = projectService;
        this.userManager = userManager;
    }

    @Override
    public Set<String> getDefaultProductGroups()
    {
        // jira-hipchat-discussions revealed that users can't modify issues if not in this group
        return GROUPS;
    }

    @Override
    public void ensureGroupHasProductAdminPermission(String groupKey)
    {
        if (!groupHasProductAdminPermission(groupKey))
        {
            jiraPermissionManager.addPermission(ADMIN_PERMISSION, groupKey);
        }
    }

    @Override
    public boolean groupHasProductAdminPermission(final String groupKey)
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

    @Override
    public void provisionAddonUserForScopes(final String userKey, final Collection<ScopeName> scopes)
    {
        // TODO: is this correct? does getAllProjects(user) only return projects that the add-on has visibility too now
        // but may have after the permission grant?!
        ApplicationUser user = userManager.getUserByKey(userKey);
        final List<Project> projects = projectService.getAllProjects(user).getReturnedValue();
        for (Project project : projects)
        {
            // todo
        }
        // TODO throw new UnsupportedOperationException("NIH");
    }
}
