package com.atlassian.plugin.connect.jira.auth;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.jira.application.ApplicationAuthorizationService;
import com.atlassian.jira.application.ApplicationRole;
import com.atlassian.jira.application.ApplicationRoleManager;
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
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonInitException;
import com.atlassian.plugin.connect.crowd.permissions.ConnectCrowdPermissions;
import com.atlassian.plugin.connect.crowd.permissions.ConnectCrowdPermissions.GrantResult;
import com.atlassian.plugin.connect.crowd.spi.CrowdAddonUserProvisioningService;
import com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddonUserGroupProvisioningService;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeUtil;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.atlassian.plugin.connect.crowd.permissions.ConnectCrowdPermissions.GrantResult.REMOTE_GRANT_FAILED;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.any;

@SuppressWarnings("unused")
@JiraComponent
@ExportAsDevService
public class JiraAddonUserProvisioningService implements CrowdAddonUserProvisioningService {
    private static final String CONNECT_PROJECT_ACCESS_PROJECT_ROLE_NAME = "atlassian-addons-project-access";
    private static final String CONNECT_PROJECT_ACCESS_PROJECT_ROLE_DESC = "A project role that represents Connect add-ons declaring a scope that requires more than read issue permissions";
    /**
     * The group which is created to house all add-on users which have administrative rights.
     */
    private static final String ADDON_ADMIN_USER_GROUP_KEY = "atlassian-addons-admin";
    private static final String ADMIN_APPLICATION_ID = "jira";
    private static final String ADMIN_APPLICATION_ID_ROLES_ENABLED = "jira-admin";
    private static final String PRODUCT_ID = "jira";

    private static final ImmutableSet<String> DEFAULT_GROUPS_ALWAYS_EXPECTED = ImmutableSet.of();
    private static final ImmutableSet<String> DEFAULT_GROUPS_ONE_OR_MORE_EXPECTED = ImmutableSet.of("jira-users", "users");

    private static final int ADMIN_PERMISSION = Permissions.ADMINISTER;

    private static final Logger log = LoggerFactory.getLogger(JiraAddonUserProvisioningService.class);

    private final GlobalPermissionManager jiraPermissionManager;
    private final PermissionSchemeManager permissionSchemeManager;
    private final ProjectManager projectManager;
    private final ProjectRoleService projectRoleService;
    private final UserManager userManager;
    private final ConnectAddonUserGroupProvisioningService connectAddonUserGroupProvisioningService;
    private final TransactionTemplate transactionTemplate;
    private final PermissionManager jiraProjectPermissionManager;
    private final ConnectCrowdPermissions connectCrowdPermissions;
    private final ApplicationAuthorizationService applicationAuthorizationService;
    private final ApplicationRoleManager applicationRoleManager;

    @Inject
    public JiraAddonUserProvisioningService(GlobalPermissionManager jiraPermissionManager,
                                            ProjectManager projectManager,
                                            UserManager userManager,
                                            PermissionSchemeManager permissionSchemeManager,
                                            ProjectRoleService projectRoleService,
                                            ConnectAddonUserGroupProvisioningService connectAddonUserGroupProvisioningService,
                                            TransactionTemplate transactionTemplate,
                                            PermissionManager jiraProjectPermissionManager,
                                            ApplicationAuthorizationService applicationAuthorizationService,
                                            ConnectCrowdPermissions connectCrowdPermissions,
                                            ApplicationRoleManager applicationRoleManager) {
        this.jiraProjectPermissionManager = jiraProjectPermissionManager;
        this.connectCrowdPermissions = connectCrowdPermissions;
        this.jiraPermissionManager = checkNotNull(jiraPermissionManager);
        this.projectManager = checkNotNull(projectManager);
        this.userManager = checkNotNull(userManager);
        this.permissionSchemeManager = checkNotNull(permissionSchemeManager);
        this.projectRoleService = checkNotNull(projectRoleService);
        this.connectAddonUserGroupProvisioningService = checkNotNull(connectAddonUserGroupProvisioningService);
        this.transactionTemplate = transactionTemplate;
        this.applicationAuthorizationService = applicationAuthorizationService;
        this.applicationRoleManager = applicationRoleManager;
    }

    @Override
    public Set<String> getDefaultProductGroupsAlwaysExpected() {
        return DEFAULT_GROUPS_ALWAYS_EXPECTED;
    }

    @Override
    public Set<String> getDefaultProductGroupsOneOrMoreExpected() {
        if (!applicationAuthorizationService.rolesEnabled()) {
            return DEFAULT_GROUPS_ONE_OR_MORE_EXPECTED;
        }

        Set<String> groupSet = new HashSet<>();
        Set<ApplicationRole> applicationRoles = applicationRoleManager.getRoles();
        if (!applicationRoles.isEmpty()) {
            for (ApplicationRole applicationRole : applicationRoles) {
                groupSet.addAll(applicationRole.getDefaultGroups().stream()
                        .map(Group::getName)
                        .collect(Collectors.toList()));
            }
        }

        if (applicationRoles.isEmpty() || groupSet.isEmpty()) {
            throw new ConnectAddonInitException("No application roles were present, we expect at least one to be on an instance");
        }
        return groupSet;
    }

    @Override
    public void provisionAddonUserForScopes(final String username, final Set<ScopeName> previousScopes, final Set<ScopeName> newScopes) throws ConnectAddonInitException {
        // Subvert permission checking while provisioning add-on users. This can be invoked on a thread with no
        // authentication context (for example, the auto-update task thread run scheduled by the UPM) so permission
        // checks would fail.
        transactionTemplate.execute(SubvertedPermissionsTransactionTemplate.subvertPermissions(() -> {
            provisionAddonUserForScopesInTransaction(username, previousScopes, newScopes);
            return null;
        }));
    }

    private void provisionAddonUserForScopesInTransaction(final String username, final Set<ScopeName> previousScopes, final Set<ScopeName> newScopes) throws ConnectAddonInitException {
        ApplicationUser user = userManager.getUserByName(username);

        if (null == user) {
            throw new IllegalArgumentException(String.format("Cannot provision non-existent user '%s': please create it first!", username));
        }

        // After a manual re-install of the add-on, there are no previous known scopes, but there could still be
        // an existing permission setup from the previous installation that needs to be removed.
        boolean removeExistingAdminPermissionSetup = previousScopes.isEmpty() && adminGroupExists();
        boolean removeExistingProjectPermissionSetup = previousScopes.isEmpty() && projectRoleExists();

        // ADMIN to x scope transition
        if (removeExistingAdminPermissionSetup || ScopeUtil.isTransitionDownFromAdmin(previousScopes, newScopes)) {
            removeUserFromGlobalAdmins(user);
        }
        // x to READ scope transition
        if (removeExistingProjectPermissionSetup || ScopeUtil.isTransitionDownToRead(previousScopes, newScopes)) {
            removeProjectPermissions(user);
        }
        // x to ADMIN scope transition
        if (ScopeUtil.isTransitionUpToAdmin(previousScopes, newScopes)) {
            makeUserGlobalAdmin(user);
        }
        // READ to x scope transition
        if (!ScopeUtil.isTransitionDownToRead(previousScopes, newScopes)) {
            updateProjectPermissions(user);
        }
    }

    private boolean projectRoleExists() {
        return null != projectRoleService.getProjectRoleByName(CONNECT_PROJECT_ACCESS_PROJECT_ROLE_NAME, new SimpleErrorCollection());
    }

    private boolean adminGroupExists() throws ConnectAddonInitException {
        try {
            return null != connectAddonUserGroupProvisioningService.findGroupByKey(ADDON_ADMIN_USER_GROUP_KEY);
        } catch (ApplicationNotFoundException | ApplicationPermissionException | InvalidAuthenticationException e) {
            throw new ConnectAddonInitException(e);
        }
    }

    private void makeUserGlobalAdmin(ApplicationUser user) throws ConnectAddonInitException {
        try {
            ensureGroupExistsAndIsAdmin(ADDON_ADMIN_USER_GROUP_KEY);
            connectAddonUserGroupProvisioningService.ensureUserIsInGroup(user.getName(), ADDON_ADMIN_USER_GROUP_KEY);
        } catch (GroupNotFoundException | ApplicationNotFoundException | OperationFailedException
                | ApplicationPermissionException | UserNotFoundException | InvalidAuthenticationException e) {
            // this should never happen because we've just "successfully" ensured that the group exists,
            // so if it does then it's programmer error and not part of this interface method's signature
            throw new ConnectAddonInitException(e);
        }
    }

    private void removeUserFromGlobalAdmins(ApplicationUser user) throws ConnectAddonInitException {
        try {
            connectAddonUserGroupProvisioningService.removeUserFromGroup(user.getName(), ADDON_ADMIN_USER_GROUP_KEY);
        } catch (OperationFailedException | ApplicationNotFoundException
                | ApplicationPermissionException | UserNotFoundException
                | GroupNotFoundException | InvalidAuthenticationException e) {
            throw new ConnectAddonInitException(e);
        }
    }

    private void ensureGroupExistsAndIsAdmin(String groupKey)
            throws ConnectAddonInitException, OperationFailedException, ApplicationNotFoundException, ApplicationPermissionException, InvalidAuthenticationException {
        final boolean created = connectAddonUserGroupProvisioningService.ensureGroupExists(groupKey);

        if (created) {
            GrantResult result = giveAdminPermission(groupKey);
            if (result == REMOTE_GRANT_FAILED) {
                throw new ConnectAddonInitException(String.format("Failed to grant '%s' administrative rights through the Remote UM REST API", groupKey));
            }
            ensureGroupHasAdminPermission(groupKey);
        } else if (!groupHasAdminPermission(groupKey)) {
            throw new ConnectAddonInitException(String.format("Group '%s' already exists and is NOT an administrators group. " +
                            "Cannot make it an administrators group because that would elevate the privileges of existing users in this group. " +
                            "Consequently, add-on users that need to be admins cannot be made admins by adding them to this group and making it an administrators group. " +
                            "Aborting user setup.",
                    groupKey), ConnectAddonInitException.ADDON_ADMINS_MISSING_PERMISSION);
        }
    }

    private GrantResult giveAdminPermission(String groupKey) {
        if (applicationAuthorizationService.rolesEnabled()) {
            return connectCrowdPermissions.giveAdminPermission(groupKey, PRODUCT_ID, ADMIN_APPLICATION_ID_ROLES_ENABLED);
        } else {
            return connectCrowdPermissions.giveAdminPermission(groupKey, PRODUCT_ID, ADMIN_APPLICATION_ID);
        }
    }

    private void ensureGroupHasAdminPermission(String groupKey) {
        if (!groupHasAdminPermission(groupKey)) {
            boolean permissionGranted = jiraPermissionManager.addPermission(ADMIN_PERMISSION, groupKey);
            if (permissionGranted) {
                log.info("Granted admin permission to group '{}'.", groupKey);
            } else {
                log.warn("Failed to grant '{}' administrative rights through the deprecated jira API", groupKey);
            }

        }
    }

    private boolean groupHasAdminPermission(final String groupKey) {
        checkNotNull(groupKey);
        return any(jiraPermissionManager.getGroupsWithPermission(ADMIN_PERMISSION),
                group -> null != group && groupKey.equals(group.getName()));
    }

    private void updateProjectPermissions(ApplicationUser addonUser) throws ConnectAddonInitException {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole projectRole = getOrCreateProjectRole(errorCollection);
        if (null != projectRole) {
            addUserToProjectRoleDefaults(addonUser, projectRole, errorCollection);
            for (Project project : getAllProjects()) {
                ProjectRoleActors roleActors = projectRoleService.getProjectRoleActors(projectRole, project, errorCollection);
                if (!roleActors.contains(addonUser)) {
                    projectRoleService.addActorsToProjectRole(
                            Collections.singleton(addonUser.getKey()),
                            projectRole,
                            project,
                            ProjectRoleActor.USER_ROLE_ACTOR_TYPE,
                            errorCollection);
                    String projectKey = null == project ? null : project.getKey();
                    log.info("Added user '{}' to project '{}' role '{}'", addonUser.getName(), projectKey, projectRole.getName());
                }
            }
        }
        if (errorCollection.hasAnyErrors()) {
            throw new ConnectAddonInitException(generateErrorMessage(errorCollection));
        }
    }

    private void removeProjectPermissions(ApplicationUser addonUser) throws ConnectAddonInitException {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole projectRole = projectRoleService.getProjectRoleByName(CONNECT_PROJECT_ACCESS_PROJECT_ROLE_NAME, errorCollection);
        if (null != projectRole) {
            removeUserFromProjectRoleDefaults(addonUser, projectRole, errorCollection);
            for (Project project : getAllProjects()) {
                projectRoleService.removeActorsFromProjectRole(
                        Collections.singleton(addonUser.getKey()),
                        projectRole,
                        project,
                        ProjectRoleActor.USER_ROLE_ACTOR_TYPE,
                        errorCollection);
                String projectKey = null == project ? null : project.getKey();
                log.info("Removed user '{}' from project '{}' role '{}'", addonUser.getName(), projectKey, projectRole.getName());
            }
        }
        if (errorCollection.hasAnyErrors()) {
            throw new ConnectAddonInitException(generateErrorMessage(errorCollection));
        }
    }

    private void addUserToProjectRoleDefaults(ApplicationUser addonUser, ProjectRole projectRole, ErrorCollection errorCollection) {
        DefaultRoleActors roleActors = projectRoleService.getDefaultRoleActors(projectRole, errorCollection);
        if (!roleActors.contains(addonUser)) {
            projectRoleService.addDefaultActorsToProjectRole(
                    Collections.singleton(addonUser.getKey()),
                    projectRole,
                    ProjectRoleActor.USER_ROLE_ACTOR_TYPE,
                    errorCollection
            );
            String projectRoleName = null == projectRole ? null : projectRole.getName();
            log.info("Added user '{}' to default project role '{}'.", addonUser.getName(), projectRoleName);
        }
    }

    private void removeUserFromProjectRoleDefaults(ApplicationUser addonUser, ProjectRole projectRole, ErrorCollection errorCollection) {
        projectRoleService.removeDefaultActorsFromProjectRole(
                Collections.singleton(addonUser.getKey()),
                projectRole,
                ProjectRoleActor.USER_ROLE_ACTOR_TYPE,
                errorCollection
        );
        String projectRoleName = null == projectRole ? null : projectRole.getName();
        log.info("Removed user '{}' from default project role '{}'.", addonUser.getName(), projectRoleName);
    }

    private ProjectRole getOrCreateProjectRole(ErrorCollection errorCollection) {
        ProjectRole projectRole = projectRoleService.getProjectRoleByName(CONNECT_PROJECT_ACCESS_PROJECT_ROLE_NAME, errorCollection);
        if (null == projectRole) {
            ProjectRole newProjectRole = new ProjectRoleImpl(CONNECT_PROJECT_ACCESS_PROJECT_ROLE_NAME, CONNECT_PROJECT_ACCESS_PROJECT_ROLE_DESC);
            projectRole = projectRoleService.createProjectRole(newProjectRole, errorCollection);
            if (null != projectRole) {
                associateProjectRoleWithPermissionSchemes(projectRole, errorCollection);
            }
        }
        return projectRole;
    }

    private void associateProjectRoleWithPermissionSchemes(ProjectRole projectRole, ErrorCollection errorCollection) {
        final String permissionType = ProjectRoleService.PROJECTROLE_PERMISSION_TYPE;
        final String parameter = projectRole.getId().toString();
        try {
            List<GenericValue> schemes = getSchemes(errorCollection);
            for (GenericValue scheme : schemes) {
                for (ProjectPermission permission : jiraProjectPermissionManager.getAllProjectPermissions()) {
                    ProjectPermissionKey permissionKey = new ProjectPermissionKey(permission.getKey());
                    if (!permissionExists(scheme, permissionKey, permissionType, parameter)) {
                        SchemeEntity schemeEntity = new SchemeEntity(permissionType, parameter, permissionKey);
                        permissionSchemeManager.createSchemeEntity(scheme, schemeEntity);
                        log.debug("Associated project role '{}' with permission scheme '{}'.", projectRole.getName(), schemeEntity.getSchemeId());
                    }
                }
            }
        } catch (GenericEntityException e) {
            String errorMessage = "Could not add project role "
                    + CONNECT_PROJECT_ACCESS_PROJECT_ROLE_NAME
                    + " to permission schemes";
            errorCollection.addErrorMessage(errorMessage);
            log.error(errorMessage, e);
        }
    }

    public List<Project> getAllProjects() {
        return projectManager.getProjectObjects();
    }

    private boolean permissionExists(GenericValue scheme, ProjectPermissionKey permissionKey, String type, String parameter) throws GenericEntityException {
        return !(permissionSchemeManager.getEntities(scheme, permissionKey, type, parameter).isEmpty());
    }

    public List<GenericValue> getSchemes(ErrorCollection errorCollection) {
        try {
            return permissionSchemeManager.getSchemes();
        } catch (GenericEntityException e) {
            errorCollection.addErrorMessage("Schemes could not be loaded and the project role "
                    + CONNECT_PROJECT_ACCESS_PROJECT_ROLE_NAME
                    + " was not added to any permission schemes");
            log.error("Error while loading schemes", e);
        }
        return ImmutableList.of();
    }

    private String generateErrorMessage(ErrorCollection errorCollection) {
        StringBuilder finalMessage = new StringBuilder();
        int counter = 0;
        for (String errorMessage : errorCollection.getErrorMessages()) {
            finalMessage.append(counter++);
            finalMessage.append("> ");
            finalMessage.append(errorMessage);
            finalMessage.append("\n");
        }
        for (Map.Entry<String, String> error : errorCollection.getErrors().entrySet()) {
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
