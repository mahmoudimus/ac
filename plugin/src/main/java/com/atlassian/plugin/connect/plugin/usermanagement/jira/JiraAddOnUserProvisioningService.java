package com.atlassian.plugin.connect.plugin.usermanagement.jira;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.permission.Permission;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserProvisioningService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.any;

@SuppressWarnings ("unused")
@JiraComponent
@ExportAsDevService
public class JiraAddOnUserProvisioningService implements ConnectAddOnUserProvisioningService
{
    private static final String CONNECT_PROJECT_ADMIN_PROJECT_ROLE_NAME = "Connect Project Admin Add-Ons";
    private static final String CONNECT_PROJECT_ADMIN_PROJECT_ROLE_DESC = "A project role that represents service users of Connect add-ons declaring Project Admin scope";

    private static final ImmutableSet<String> GROUPS = ImmutableSet.of("jira-users");
    private static final int ADMIN_PERMISSION = Permissions.ADMINISTER;

    private static final Logger log = LoggerFactory.getLogger(JiraAddOnUserProvisioningService.class);

    private final GlobalPermissionManager jiraPermissionManager;
    private final PermissionSchemeManager permissionSchemeManager;
    private final ProjectManager projectManager;
    private final ProjectRoleService projectRoleService;
    private final UserManager userManager;

    @Inject
    public JiraAddOnUserProvisioningService(GlobalPermissionManager jiraPermissionManager, ProjectManager projectManager,
            UserManager userManager, PermissionSchemeManager permissionSchemeManager, ProjectRoleService projectRoleService)
    {
        this.jiraPermissionManager = checkNotNull(jiraPermissionManager);
        this.projectManager = projectManager;
        this.userManager = userManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.projectRoleService = projectRoleService;
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
    public void provisionAddonUserForScopes(final String userKey, final Set<ScopeName> scopes)
    {
        ApplicationUser user = userManager.getUserByKey(userKey);
        // Assuming normalized set of scopes
        if (scopes.contains(ScopeName.PROJECT_ADMIN))
        {
            updateProjectAdminScopePermissions(user);
        }
        else
        {
            removeProjectAdminScopePermissions(user);
        }
    }

    private void updateProjectAdminScopePermissions(ApplicationUser addOnUser)
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole projectRole = updateConnectProjectRole(errorCollection);
        if (null != projectRole)
        {
            for (Project project : getAllProjects())
            {
                projectRoleService.addActorsToProjectRole(
                        Collections.singleton(addOnUser.getName()),
                        projectRole,
                        project,
                        UserRoleActorFactory.TYPE,
                        errorCollection);
            }
            addUserToProjectRoleDefaults(addOnUser, projectRole, errorCollection);
        }
        // TODO: throw the error collection back to the installer
    }

    private void addUserToProjectRoleDefaults(ApplicationUser addOnUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        projectRoleService.addDefaultActorsToProjectRole(
                Collections.singleton(addOnUser.getName()),
                projectRole,
                UserRoleActorFactory.TYPE,
                errorCollection
        );
    }

    private void removeProjectAdminScopePermissions(ApplicationUser addOnUser)
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole projectRole = projectRoleService.getProjectRoleByName(CONNECT_PROJECT_ADMIN_PROJECT_ROLE_NAME, errorCollection);
        if (null != projectRole)
        {
            for (Project project : getAllProjects())
            {
                projectRoleService.removeActorsFromProjectRole(
                        Collections.singleton(addOnUser.getName()),
                        projectRole,
                        project,
                        UserRoleActorFactory.TYPE,
                        errorCollection);
            }
            removeUserFromProjectRoleDefaults(addOnUser, projectRole, errorCollection);
        }
        // TODO: throw the error collection back to the installer
    }

    private void removeUserFromProjectRoleDefaults(ApplicationUser addOnUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        projectRoleService.removeDefaultActorsFromProjectRole(
                Collections.singleton(addOnUser.getName()),
                projectRole,
                UserRoleActorFactory.TYPE,
                errorCollection
        );
    }

    private ProjectRole updateConnectProjectRole(ErrorCollection errorCollection)
    {
        ProjectRole projectRole = projectRoleService.getProjectRoleByName(CONNECT_PROJECT_ADMIN_PROJECT_ROLE_NAME, errorCollection);
        if (null == projectRole)
        {
            ProjectRole newProjectRole = new ProjectRoleImpl(CONNECT_PROJECT_ADMIN_PROJECT_ROLE_NAME, CONNECT_PROJECT_ADMIN_PROJECT_ROLE_DESC);
            projectRole = projectRoleService.createProjectRole(newProjectRole, errorCollection);
            if (null != projectRole)
            {
                associateProjectRoleWithPermissionSchemes(projectRole, errorCollection);
            }
        }
        return projectRole;
    }

    private void associateProjectRoleWithPermissionSchemes(ProjectRole projectRole, ErrorCollection errorCollection)
    {
        final long permission = Permission.PROJECT_ADMIN.getId();
        final String permissionType = ProjectRoleService.PROJECTROLE_PERMISSION_TYPE;
        final String parameter = projectRole.getId().toString();

        List<GenericValue> schemes = getSchemes(errorCollection);
        for (GenericValue scheme : schemes)
        {
            try
            {
                if (!permissionExists(scheme, permission, permissionType, parameter))
                {
                    SchemeEntity schemeEntity = new SchemeEntity(permissionType, parameter, permission);
                    permissionSchemeManager.createSchemeEntity(scheme, schemeEntity);
                }
            }
            catch (GenericEntityException e)
            {
                String errorMessage = "Could not add project role " + CONNECT_PROJECT_ADMIN_PROJECT_ROLE_NAME +
                        " to permission scheme '" + scheme.getString("name") + "'";
                errorCollection.addErrorMessage(errorMessage);
                log.error(errorMessage, e);
            }
        }
    }

    public List<Project> getAllProjects()
    {
        return projectManager.getProjectObjects();
    }

    private boolean permissionExists(GenericValue scheme, Long permission, String type, String parameter) throws GenericEntityException
    {
        return !(permissionSchemeManager.getEntities(scheme, permission, type, parameter).isEmpty());
    }

    public List<GenericValue> getSchemes(ErrorCollection errorCollection)
    {
        try
        {
            return permissionSchemeManager.getSchemes();
        }
        catch (GenericEntityException e)
        {
            errorCollection.addErrorMessage("Schemes could not be loaded and the project role "
                    + CONNECT_PROJECT_ADMIN_PROJECT_ROLE_NAME
                    + " was not added to any permission schemes");
            log.error("Error while loading schemes", e);
        }
        return ImmutableList.of();
    }
}
