package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.permission.Permission;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class JiraConnectAddOnUserProvisioningService
{
    private static final ImmutableSet<String> GROUPS = ImmutableSet.of("jira-users");

    private static final String CONNECT_PROJECT_ADMIN_PROJECT_ROLE_NAME = "Connect Project Admin Add-Ons";
    private static final String CONNECT_PROJECT_ADMIN_PROJECT_ROLE_DESC = "A project role that represents service users of Connect add-ons declaring Project Admin scope";

    private static final Logger log = LoggerFactory.getLogger(JiraConnectAddOnUserGroupsService.class);

    private ProjectRoleService projectRoleService;
    private ProjectManager projectManager;
    private PermissionSchemeManager permissionSchemeManager;

    public Set<String> getDefaultProductGroups()
    {
        // jira-hipchat-discussions revealed that users can't modify issues if not in this group
        return GROUPS;
    }

    public void establishScopePermissions(User addOnUser, ScopeName scope)
    {
        if (ScopeName.PROJECT_ADMIN.equals(scope))
        {
            establishProjectAdminScopePermissions(addOnUser);
        }
    }

    public void removeScopePermissions(User addOnUser, ScopeName scope)
    {
        if (ScopeName.PROJECT_ADMIN.equals(scope))
        {
            removeProjectAdminScopePermissions(addOnUser);
        }
    }

    private void establishProjectAdminScopePermissions(User addOnUser)
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole projectRole = establishConnectProjectRole(errorCollection);
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
        }
        // TODO: get the error collection back to the installer
    }

    private void removeProjectAdminScopePermissions(User addOnUser)
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
        }
        // TODO: get the error collection back to the installer
    }

    private ProjectRole establishConnectProjectRole(ErrorCollection errorCollection)
    {
        ProjectRole projectRole = projectRoleService.getProjectRoleByName(CONNECT_PROJECT_ADMIN_PROJECT_ROLE_NAME, errorCollection);
        if (null == projectRole)
        {
            ProjectRole newProjectRole = new ProjectRoleImpl(CONNECT_PROJECT_ADMIN_PROJECT_ROLE_NAME, CONNECT_PROJECT_ADMIN_PROJECT_ROLE_DESC);
            projectRole = projectRoleService.createProjectRole(newProjectRole, errorCollection);
            if (null != projectRole && !errorCollection.hasAnyErrors())
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
