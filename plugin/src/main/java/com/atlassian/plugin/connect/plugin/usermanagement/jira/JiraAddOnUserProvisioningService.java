package com.atlassian.plugin.connect.plugin.usermanagement.jira;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.exception.*;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.permission.Permission;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.DefaultRoleActors;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserInitException;
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
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.any;

@SuppressWarnings ("unused")
@JiraComponent
@ExportAsDevService
public class JiraAddOnUserProvisioningService implements ConnectAddOnUserProvisioningService
{
    private static final String CONNECT_PROJECT_ADMIN_PROJECT_ROLE_NAME = "atlassian-addons-project-admin";
    private static final String CONNECT_PROJECT_ADMIN_PROJECT_ROLE_DESC = "A project role that represents Connect add-ons declaring Project Admin scope";
    private static final String ATLASSIAN_ADDONS_ADMIN_GROUP_KEY = "atlassian-addons-admin";

    private static final ImmutableSet<String> GROUPS = ImmutableSet.of("jira-users");
    private static final int ADMIN_PERMISSION = Permissions.ADMINISTER;

    private static final Logger log = LoggerFactory.getLogger(JiraAddOnUserProvisioningService.class);

    private final GlobalPermissionManager jiraPermissionManager;
    private final PermissionSchemeManager permissionSchemeManager;
    private final ProjectManager projectManager;
    private final ProjectRoleService projectRoleService;
    private final UserManager userManager;
    private final ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService;

    @Inject
    public JiraAddOnUserProvisioningService(GlobalPermissionManager jiraPermissionManager,
                                            ProjectManager projectManager,
                                            UserManager userManager,
                                            PermissionSchemeManager permissionSchemeManager,
                                            ProjectRoleService projectRoleService,
                                            ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService)
    {
        this.jiraPermissionManager = checkNotNull(jiraPermissionManager);
        this.projectManager = checkNotNull(projectManager);
        this.userManager = checkNotNull(userManager);
        this.permissionSchemeManager = checkNotNull(permissionSchemeManager);
        this.projectRoleService = checkNotNull(projectRoleService);
        this.connectAddOnUserGroupProvisioningService = checkNotNull(connectAddOnUserGroupProvisioningService);
    }

    @Override
    public Set<String> getDefaultProductGroups()
    {
        // jira-hipchat-discussions revealed that users can't modify issues if not in this group
        return GROUPS;
    }

    @Override
    public void provisionAddonUserForScopes(final String userKey, final Set<ScopeName> previousScopes, final Set<ScopeName> newScopes) throws ConnectAddOnUserInitException
    {
        Set<ScopeName> normalizedPreviousScopes = ScopeName.normalize(previousScopes);
        Set<ScopeName> normalizedNewScopes = ScopeName.normalize(newScopes);

        ApplicationUser user = userManager.getUserByKey(userKey);

        if (null == user)
        {
            throw new IllegalArgumentException(String.format("Cannot provision non-existent user '%s': please create it first!", userKey));
        }

        // x to ADMIN scope transition
        if (normalizedNewScopes.contains(ScopeName.ADMIN))
        {
            makeUserGlobalAdmin(user);
        }
        // x to PROJECT_ADMIN scope transition
        else if (normalizedNewScopes.contains(ScopeName.PROJECT_ADMIN)
                && (!normalizedPreviousScopes.contains(ScopeName.PROJECT_ADMIN) || normalizedPreviousScopes.contains(ScopeName.ADMIN)))
        {
            updateProjectAdminScopePermissions(user);
        }

        // ADMIN to x scope transition
        if (normalizedPreviousScopes.contains(ScopeName.ADMIN) && !normalizedNewScopes.contains(ScopeName.ADMIN))
        {
            removeUserFromGlobalAdmins(user);
        }
        // PROJECT_ADMIN to x scope transition
        else if (normalizedPreviousScopes.contains(ScopeName.PROJECT_ADMIN)
                && (!normalizedNewScopes.contains(ScopeName.PROJECT_ADMIN) || normalizedNewScopes.contains(ScopeName.ADMIN)))
        {
            removeProjectAdminScopePermissions(user);
        }
    }

    private void makeUserGlobalAdmin(ApplicationUser user) throws ConnectAddOnUserInitException
    {
        try
        {
            ensureGroupExistsAndIsAdmin(ATLASSIAN_ADDONS_ADMIN_GROUP_KEY);
            connectAddOnUserGroupProvisioningService.ensureUserIsInGroup(user.getName(), ATLASSIAN_ADDONS_ADMIN_GROUP_KEY);
        }
        catch (GroupNotFoundException e)
        {
            // this should never happen because we've just "successfully" ensured that the group exists,
            // so if it does then it's programmer error and not part of this interface method's signature
            throw new ConnectAddOnUserInitException(e);
        }
        catch (UserNotFoundException e)
        {
            // this should never happen because we've just "successfully" ensured that the user exists,
            // so if it does then it's programmer error and not part of this interface method's signature
            throw new ConnectAddOnUserInitException(e);
        }
        catch (ApplicationPermissionException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
        catch (OperationFailedException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
        catch (ApplicationNotFoundException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
    }

    private void removeUserFromGlobalAdmins(ApplicationUser user) throws ConnectAddOnUserInitException
    {
        try
        {
            connectAddOnUserGroupProvisioningService.removeUserFromGroup(user.getName(), ATLASSIAN_ADDONS_ADMIN_GROUP_KEY);
        }
        catch (OperationFailedException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
        catch (ApplicationNotFoundException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
        catch (ApplicationPermissionException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
        catch (UserNotFoundException e)
        {
            // shouldn't happen
            throw new ConnectAddOnUserInitException(e);
        }
        catch (GroupNotFoundException e)
        {
            // someone removed the group, so we can't guarantee that the user is no longer admin
            throw new ConnectAddOnUserInitException(e);
        }
    }

    private void ensureGroupExistsAndIsAdmin(String groupKey) throws ConnectAddOnUserInitException, OperationFailedException, ApplicationNotFoundException, ApplicationPermissionException
    {
        final boolean created = connectAddOnUserGroupProvisioningService.ensureGroupExists(groupKey);

        if (created)
        {
            ensureGroupHasProductAdminPermission(groupKey);
        }
        else if (!groupHasProductAdminPermission(groupKey))
        {
            throw new ConnectAddOnUserInitException(String.format("Group '%s' already exists and is NOT an administrators group. " +
                    "Cannot make it an administrators group because that would elevate the privileges of existing users in this group. " +
                    "Consequently, add-on users that need to be admins cannot be made admins by adding them to this group and making it an administrators group. " +
                    "Aborting user setup.",
                    groupKey));
        }
    }

    private void ensureGroupHasProductAdminPermission(String groupKey)
    {
        if (!groupHasProductAdminPermission(groupKey))
        {
            jiraPermissionManager.addPermission(ADMIN_PERMISSION, groupKey);
        }
    }

    private boolean groupHasProductAdminPermission(final String groupKey)
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

    private void updateProjectAdminScopePermissions(ApplicationUser addOnUser) throws ConnectAddOnUserInitException
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole projectRole = updateConnectProjectRole(errorCollection);
        if (null != projectRole)
        {
            addUserToProjectRoleDefaults(addOnUser, projectRole, errorCollection);
            for (Project project : getAllProjects())
            {
                ProjectRoleActors roleActors = projectRoleService.getProjectRoleActors(projectRole, project, errorCollection);
                if (!roleActors.contains(addOnUser))
                {
                    projectRoleService.addActorsToProjectRole(
                            Collections.singleton(addOnUser.getName()),
                            projectRole,
                            project,
                            UserRoleActorFactory.TYPE,
                            errorCollection);
                }
            }
        }
        if (errorCollection.hasAnyErrors())
        {
            throw new ConnectAddOnUserInitException(generateErrorMessage(errorCollection));
        }
    }

    private void addUserToProjectRoleDefaults(ApplicationUser addOnUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        DefaultRoleActors roleActors = projectRoleService.getDefaultRoleActors(projectRole, errorCollection);
        if (!roleActors.contains(addOnUser))
        {
            projectRoleService.addDefaultActorsToProjectRole(
                    Collections.singleton(addOnUser.getName()),
                    projectRole,
                    UserRoleActorFactory.TYPE,
                    errorCollection
            );
        }
    }

    private void removeProjectAdminScopePermissions(ApplicationUser addOnUser) throws ConnectAddOnUserInitException
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole projectRole = projectRoleService.getProjectRoleByName(CONNECT_PROJECT_ADMIN_PROJECT_ROLE_NAME, errorCollection);
        if (null != projectRole)
        {
            removeUserFromProjectRoleDefaults(addOnUser, projectRole, errorCollection);
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
        if (errorCollection.hasAnyErrors())
        {
            throw new ConnectAddOnUserInitException(generateErrorMessage(errorCollection));
        }
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

    private String generateErrorMessage(ErrorCollection errorCollection)
    {
        StringBuilder finalMessage = new StringBuilder();
        int counter = 0;
        for (String errorMessage : errorCollection.getErrorMessages())
        {
            finalMessage.append(counter++);
            finalMessage.append("> ");
            finalMessage.append(errorMessage);
            finalMessage.append("\n");
        }
        for (Map.Entry<String, String> error : errorCollection.getErrors().entrySet())
        {
            finalMessage.append(counter++);
            finalMessage.append("> ");
            finalMessage.append(error.getKey());
            finalMessage.append(": ");
            finalMessage.append(error.getValue());
            finalMessage.append("\n");
        }
        return finalMessage.toString();
    }
}
