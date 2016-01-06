package com.atlassian.plugin.connect.jira.web.context;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.GlobalPermissionType;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.connect.api.web.DynamicUriVariableResolver;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class PermissionContextVariableResolver implements DynamicUriVariableResolver
{
    private interface PermissionResolver
    {
        Optional<Boolean> resolve(String addOnKey, String permissionKey, Map<String, ?> context);
    }

    private static final String PROJECT_PERMISSION_PREFIX = "projectPermission.";
    private static final String GLOBABL_PERMISSION_PREFIX = "globalPermission.";

    private final Map<String, PermissionResolver> resolvers = ImmutableMap.of(
            PROJECT_PERMISSION_PREFIX, this::resolveProjectPermission,
            GLOBABL_PERMISSION_PREFIX, this::resolveGlobalPermission
    );

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final PermissionManager permissionManager;
    private final GlobalPermissionManager globalPermissionManager;
    private final ProjectManager projectManager;
    private final IssueManager issueManager;

    @Autowired
    public PermissionContextVariableResolver(final JiraAuthenticationContext jiraAuthenticationContext, final PermissionManager permissionManager, final GlobalPermissionManager globalPermissionManager, final ProjectManager projectManager, final IssueManager issueManager)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.permissionManager = permissionManager;
        this.globalPermissionManager = globalPermissionManager;
        this.projectManager = projectManager;
        this.issueManager = issueManager;
    }

    public Optional<String> resolve(final String addOnKey, String variable, Map<String, ?> context)
    {
        return resolvers.entrySet().stream()
                .filter(entry -> variable.startsWith(entry.getKey()))
                .findFirst()
                .flatMap(resolver -> {
                    String permissionKey = variable.substring(resolver.getKey().length());
                    return resolver.getValue().resolve(addOnKey, permissionKey, context);
                })
                .map(String::valueOf);
    }

    private Optional<Boolean> resolveProjectPermission(String addOnKey, String permissionKey, Map<String, ?> context)
    {
        Collection<ProjectPermission> allPermissions = permissionManager.getAllProjectPermissions();

        Optional<ProjectPermission> fullNameMatch = allPermissions.stream().filter(permission ->
                permission.getKey().equalsIgnoreCase(permissionKey)).findFirst();
        Optional<ProjectPermission> implicitAddOnPrefixMatch = allPermissions.stream().filter(permission ->
                permission.getKey().startsWith(addOnKey + "__") && permission.getKey().substring(addOnKey.length() + 2).equalsIgnoreCase(permissionKey)).findFirst();

        Optional<ProjectPermissionKey> projectPermissionKey = Stream.of(fullNameMatch, implicitAddOnPrefixMatch)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ProjectPermission::getProjectPermissionKey)
                .findFirst();

        return projectPermissionKey.flatMap(key -> {

            ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();

            Object projectId = context.get("project.id");
            Object issueId = context.get("issue.id");
            if (projectId instanceof String)
            {
                Project project = projectManager.getProjectObj(Long.valueOf((String) projectId));
                return Optional.of(permissionManager.hasPermission(key, project, user));
            }
            else if (issueId instanceof String)
            {
                Issue issue = issueManager.getIssueObject(Long.valueOf((String) issueId));
                return Optional.of(permissionManager.hasPermission(key, issue, user));
            }
            else
            {
                return Optional.empty(); // we can't resolve because we are not in the project context
            }
        });
    }

    private Optional<Boolean> resolveGlobalPermission(String addOnKey, String permissionKey, Map<String, ?> context)
    {
        Collection<GlobalPermissionType> allPermissions = globalPermissionManager.getAllGlobalPermissions();

        Optional<GlobalPermissionType> fullNameMatch = allPermissions.stream().filter(permission ->
                permission.getKey().equalsIgnoreCase(permissionKey)).findFirst();
        Optional<GlobalPermissionType> implicitAddOnPrefixMatch = allPermissions.stream().filter(permission ->
                permission.getKey().startsWith(addOnKey + "__") && permission.getKey().substring(addOnKey.length() + 2).equalsIgnoreCase(permissionKey)).findFirst();

        Optional<GlobalPermissionKey> globalPermissionKey = Stream.of(fullNameMatch, implicitAddOnPrefixMatch)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(GlobalPermissionType::getGlobalPermissionKey)
                .findFirst();

        return globalPermissionKey.map(key -> globalPermissionManager.hasPermission(key, jiraAuthenticationContext.getLoggedInUser()));
    }
}
