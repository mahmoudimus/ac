package com.atlassian.plugin.connect.jira.usermanagement;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.security.roles.DefaultRoleActors;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserProvisioningService;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeUtil;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserKey;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.any;

@SuppressWarnings("unused")
@JiraComponent
@ExportAsDevService
public class JiraAddOnUserProvisioningService implements ConnectAddOnUserProvisioningService
{
    private static final String CONNECT_PROJECT_ACCESS_PROJECT_ROLE_NAME = "atlassian-addons-project-access";
    private static final String CONNECT_PROJECT_ACCESS_PROJECT_ROLE_DESC = "A project role that represents Connect add-ons declaring a scope that requires more than read issue permissions";
    /**
     * The group which is created to house all add-on users which have administrative rights.
     */
    private static final String ADDON_ADMIN_USER_GROUP_KEY = "atlassian-addons-admin";

    private static final ImmutableSet<String> DEFAULT_GROUPS_ALWAYS_EXPECTED = ImmutableSet.of();
    private static final ImmutableSet<String> DEFAULT_GROUPS_ONE_OR_MORE_EXPECTED = ImmutableSet.of("jira-users", "users");

    private static final int ADMIN_PERMISSION = Permissions.ADMINISTER;

    private static final Logger log = LoggerFactory.getLogger(JiraAddOnUserProvisioningService.class);

    private final GlobalPermissionManager jiraPermissionManager;
    private final PermissionSchemeManager permissionSchemeManager;
    private final ProjectManager projectManager;
    private final ProjectRoleService projectRoleService;
    private final UserManager userManager;
    private final ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService;
    private final TransactionTemplate transactionTemplate;
    private final PermissionManager jiraProjectPermissionManager;

    @Inject
    public JiraAddOnUserProvisioningService(GlobalPermissionManager jiraPermissionManager,
            ProjectManager projectManager,
            UserManager userManager,
            PermissionSchemeManager permissionSchemeManager,
            ProjectRoleService projectRoleService,
            ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService,
            TransactionTemplate transactionTemplate,
            PermissionManager jiraProjectPermissionManager)
    {
        this.jiraProjectPermissionManager = jiraProjectPermissionManager;
        this.jiraPermissionManager = checkNotNull(jiraPermissionManager);
        this.projectManager = checkNotNull(projectManager);
        this.userManager = checkNotNull(userManager);
        this.permissionSchemeManager = checkNotNull(permissionSchemeManager);
        this.projectRoleService = checkNotNull(projectRoleService);
        this.connectAddOnUserGroupProvisioningService = checkNotNull(connectAddOnUserGroupProvisioningService);
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public Set<String> getDefaultProductGroupsAlwaysExpected()
    {
        return DEFAULT_GROUPS_ALWAYS_EXPECTED;
    }

    @Override
    public Set<String> getDefaultProductGroupsOneOrMoreExpected()
    {
        return DEFAULT_GROUPS_ONE_OR_MORE_EXPECTED;
    }

    @Override
    public void provisionAddonUserForScopes(final UserKey userKey, final Set<ScopeName> previousScopes, final Set<ScopeName> newScopes) throws ConnectAddOnUserInitException
    {
        // Subvert permission checking while provisioning add-on users. This can be invoked on a thread with no
        // authentication context (for example, the auto-update task thread run scheduled by the UPM) so permission
        // checks would fail.
        transactionTemplate.execute(SubvertedPermissionsTransactionTemplate.subvertPermissions(new TransactionCallback<Void>()
        {
            @Override
            public Void doInTransaction()
            {
                provisionAddonUserForScopesInTransaction(userKey, previousScopes, newScopes);
                return null;
            }
        }));
    }

    private void provisionAddonUserForScopesInTransaction(final UserKey userKey, final Set<ScopeName> previousScopes, final Set<ScopeName> newScopes) throws ConnectAddOnUserInitException
    {
        ApplicationUser user = userManager.getUserByName(userKey.getStringValue());

        if (null == user)
        {
            throw new IllegalArgumentException(String.format("Cannot provision non-existent user '%s': please create it first!", userKey));
        }

        // After a manual re-install of the add-on, there are no previous known scopes, but there could still be
        // an existing permission setup from the previous installation that needs to be removed.
        boolean removeExistingAdminPermissionSetup = previousScopes.isEmpty() && adminGroupExists();
        boolean removeExistingProjectPermissionSetup = previousScopes.isEmpty() && projectRoleExists();

        // ADMIN to x scope transition
        if (removeExistingAdminPermissionSetup || ScopeUtil.isTransitionDownFromAdmin(previousScopes, newScopes))
        {
            removeUserFromGlobalAdmins(user);
        }
        // x to READ scope transition
        if (removeExistingProjectPermissionSetup || ScopeUtil.isTransitionDownToRead(previousScopes, newScopes))
        {
            removeProjectPermissions(user);
        }
        // x to ADMIN scope transition
        if (ScopeUtil.isTransitionUpToAdmin(previousScopes, newScopes))
        {
            makeUserGlobalAdmin(user);
        }
        // READ to x scope transition
        if (!ScopeUtil.isTransitionDownToRead(previousScopes, newScopes))
        {
            updateProjectPermissions(user);
        }
    }

    private boolean projectRoleExists()
    {
        return null != projectRoleService.getProjectRoleByName(CONNECT_PROJECT_ACCESS_PROJECT_ROLE_NAME, new SimpleErrorCollection());
    }

    private boolean adminGroupExists() throws ConnectAddOnUserInitException
    {
        try
        {
            return null != connectAddOnUserGroupProvisioningService.findGroupByKey(ADDON_ADMIN_USER_GROUP_KEY);
        }
        catch (ApplicationNotFoundException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
    }

    private void makeUserGlobalAdmin(ApplicationUser user) throws ConnectAddOnUserInitException
    {
        try
        {
            ensureGroupExistsAndIsAdmin(ADDON_ADMIN_USER_GROUP_KEY);
            connectAddOnUserGroupProvisioningService.ensureUserIsInGroup(user.getName(), ADDON_ADMIN_USER_GROUP_KEY);
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
            connectAddOnUserGroupProvisioningService.removeUserFromGroup(user.getName(), ADDON_ADMIN_USER_GROUP_KEY);
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
            // someone removed the group, which shouldn't happen
            throw new ConnectAddOnUserInitException(e);
        }
    }

    private void ensureGroupExistsAndIsAdmin(String groupKey) throws ConnectAddOnUserInitException, OperationFailedException, ApplicationNotFoundException, ApplicationPermissionException
    {
        final boolean created = connectAddOnUserGroupProvisioningService.ensureGroupExists(groupKey);

        if (created)
        {
            ensureGroupHasAdminPermission(groupKey);
        }
        else if (!groupHasAdminPermission(groupKey))
        {
            throw new ConnectAddOnUserInitException(String.format("Group '%s' already exists and is NOT an administrators group. " +
                    "Cannot make it an administrators group because that would elevate the privileges of existing users in this group. " +
                    "Consequently, add-on users that need to be admins cannot be made admins by adding them to this group and making it an administrators group. " +
                    "Aborting user setup.",
                    groupKey), ConnectAddOnUserProvisioningService.ADDON_ADMINS_MISSING_PERMISSION);
        }
    }

    private void ensureGroupHasAdminPermission(String groupKey)
    {
        if (!groupHasAdminPermission(groupKey))
        {
            jiraPermissionManager.addPermission(ADMIN_PERMISSION, groupKey);
            log.info("Granted admin permission to group '{}'.", groupKey);
        }
    }

    private boolean groupHasAdminPermission(final String groupKey)
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

    private void updateProjectPermissions(ApplicationUser addOnUser) throws ConnectAddOnUserInitException
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole projectRole = getOrCreateProjectRole(errorCollection);
        if (null != projectRole)
        {
            addUserToProjectRoleDefaults(addOnUser, projectRole, errorCollection);
            for (Project project : getAllProjects())
            {
                ProjectRoleActors roleActors = projectRoleService.getProjectRoleActors(projectRole, project, errorCollection);
                if (!roleActors.contains(addOnUser))
                {
                    projectRoleService.addActorsToProjectRole(
                            Collections.singleton(addOnUser.getKey()),
                            projectRole,
                            project,
                            ProjectRoleActor.USER_ROLE_ACTOR_TYPE,
                            errorCollection);
                    String projectKey = null == project ? null : project.getKey();
                    log.info("Added user '{}' to project '{}' role '{}'", new Object[]{ addOnUser.getName(), projectKey, projectRole.getName() });
                }
            }
        }
        if (errorCollection.hasAnyErrors())
        {
            throw new ConnectAddOnUserInitException(generateErrorMessage(errorCollection));
        }
    }

    private void removeProjectPermissions(ApplicationUser addOnUser) throws ConnectAddOnUserInitException
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole projectRole = projectRoleService.getProjectRoleByName(CONNECT_PROJECT_ACCESS_PROJECT_ROLE_NAME, errorCollection);
        if (null != projectRole)
        {
            removeUserFromProjectRoleDefaults(addOnUser, projectRole, errorCollection);
            for (Project project : getAllProjects())
            {
                projectRoleService.removeActorsFromProjectRole(
                        Collections.singleton(addOnUser.getKey()),
                        projectRole,
                        project,
                        ProjectRoleActor.USER_ROLE_ACTOR_TYPE,
                        errorCollection);
                String projectKey = null == project ? null : project.getKey();
                log.info("Removed user '{}' from project '{}' role '{}'", new Object[]{ addOnUser.getName(), projectKey, projectRole.getName() });
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
                    Collections.singleton(addOnUser.getKey()),
                    projectRole,
                    ProjectRoleActor.USER_ROLE_ACTOR_TYPE,
                    errorCollection
            );
            String projectRoleName = null == projectRole ? null : projectRole.getName();
            log.info("Added user '{}' to default project role '{}'.", addOnUser.getName(), projectRoleName);
        }
    }

    private void removeUserFromProjectRoleDefaults(ApplicationUser addOnUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        projectRoleService.removeDefaultActorsFromProjectRole(
                Collections.singleton(addOnUser.getKey()),
                projectRole,
                ProjectRoleActor.USER_ROLE_ACTOR_TYPE,
                errorCollection
        );
        String projectRoleName = null == projectRole ? null : projectRole.getName();
        log.info("Removed user '{}' from default project role '{}'.", addOnUser.getName(), projectRoleName);
    }

    private ProjectRole getOrCreateProjectRole(ErrorCollection errorCollection)
    {
        ProjectRole projectRole = projectRoleService.getProjectRoleByName(CONNECT_PROJECT_ACCESS_PROJECT_ROLE_NAME, errorCollection);
        if (null == projectRole)
        {
            ProjectRole newProjectRole = new ProjectRoleImpl(CONNECT_PROJECT_ACCESS_PROJECT_ROLE_NAME, CONNECT_PROJECT_ACCESS_PROJECT_ROLE_DESC);
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
        final String permissionType = ProjectRoleService.PROJECTROLE_PERMISSION_TYPE;
        final String parameter = projectRole.getId().toString();
        try
        {
            List<GenericValue> schemes = getSchemes(errorCollection);
            for (GenericValue scheme : schemes)
            {
                for (ProjectPermission permission : jiraProjectPermissionManager.getAllProjectPermissions())
                {
                    ProjectPermissionKey permissionKey = new ProjectPermissionKey(permission.getKey());
                    if (!permissionExists(scheme, permissionKey, permissionType, parameter))
                    {
                        SchemeEntity schemeEntity = new SchemeEntity(permissionType, parameter, permissionKey);
                        permissionSchemeManager.createSchemeEntity(scheme, schemeEntity);
                        log.debug("Associated project role '{}' with permission scheme '{}'.", projectRole.getName(), schemeEntity.getSchemeId());
                    }
                }
            }
        }
        catch (GenericEntityException e)
        {
            String errorMessage = "Could not add project role "
                    + CONNECT_PROJECT_ACCESS_PROJECT_ROLE_NAME
                    + " to permission schemes";
            errorCollection.addErrorMessage(errorMessage);
            log.error(errorMessage, e);
        }
    }

    public List<Project> getAllProjects()
    {
        return projectManager.getProjectObjects();
    }

    private boolean permissionExists(GenericValue scheme, ProjectPermissionKey permissionKey, String type, String parameter) throws GenericEntityException
    {
        return !(permissionSchemeManager.getEntities(scheme, permissionKey, type, parameter).isEmpty());
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
                    + CONNECT_PROJECT_ACCESS_PROJECT_ROLE_NAME
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
