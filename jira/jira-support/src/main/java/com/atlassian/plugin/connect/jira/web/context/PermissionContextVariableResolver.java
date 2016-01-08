package com.atlassian.plugin.connect.jira.web.context;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.atlassian.fugue.Maybe;
import com.atlassian.fugue.Option;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.connect.spi.web.context.DynamicUriVariableResolver;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;

/**
 * A component that can resolve variables of the form {@code [project|global]Permission.<permissionKey>}.
 *
 * <p>
 *  If the permission with the given key does not exist or it's not possible to resolve a project
 *  permission because we are not in the project context, then the resolver will not return anything.
 *
 * <p>
 *     This resolver is trying to be smart in two ways:
 *     <ul>
 *         <li>it is case-insensitive for JIRA built-in permission keys</li>
 *         <li>for custom permission keys, the add-on does not have to (but may if it wishes so) prefix them with {@code <addOnKey>__} in the descriptor</li>
 *     </ul>
 */
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
        Optional<ProjectPermissionKey> projectPermissionKey = findPermissionKey(addOnKey, permissionKey,
                ProjectPermissionKey::new,
                permissionManager::getProjectPermission);

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
        Optional<GlobalPermissionKey> globalPermissionKey = findPermissionKey(addOnKey, permissionKey,
                GlobalPermissionKey::of,
                globalPermissionManager::getGlobalPermission);

        return globalPermissionKey.map(key -> globalPermissionManager.hasPermission(key, jiraAuthenticationContext.getLoggedInUser()));
    }

    private <Key> Optional<Key> findPermissionKey(String addOnKey, String exactKey, Function<String, Key> keyConstructor, Function<Key, Option<?>> keyGetter)
    {
        String upperCaseKey = exactKey.toUpperCase();
        String implicitAddOnPrefix = addonAndModuleKey(addOnKey, exactKey);

        Stream<String> possibleMatches = Stream.of(exactKey, upperCaseKey, implicitAddOnPrefix);
        Predicate<String> isKeyDefined = keyConstructor.andThen(keyGetter).andThen(Maybe::isDefined)::apply;

        return possibleMatches.filter(isKeyDefined).findFirst().map(keyConstructor);
    }
}
