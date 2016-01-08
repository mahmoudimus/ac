package com.atlassian.plugin.connect.jira.web.context;

import java.util.HashMap;
import java.util.Optional;

import com.atlassian.fugue.Option;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.GlobalPermissionType;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import org.junit.Before;
import org.junit.Test;

import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PermissionContextVariableResolverTest
{
    private final static ApplicationUser USER = new MockApplicationUser("currentUser");
    private final static String PROJECT_ID = "42";
    private final static String ISSUE_ID = "42";
    private final static Project PROJECT = new MockProject(Long.valueOf(PROJECT_ID));
    private final static MutableIssue ISSUE = new MockIssue(Long.valueOf(ISSUE_ID));

    private final JiraAuthenticationContext jiraAuthenticationContext = mock(JiraAuthenticationContext.class);
    private final PermissionManager permissionManager = mock(PermissionManager.class);
    private final GlobalPermissionManager globalPermissionManager = mock(GlobalPermissionManager.class);
    private final ProjectManager projectManager = mock(ProjectManager.class);
    private final IssueManager issueManager = mock(IssueManager.class);

    private final PermissionContextVariableResolver resolver = new PermissionContextVariableResolver(
            jiraAuthenticationContext, permissionManager, globalPermissionManager, projectManager, issueManager
    );

    @Before
    public void setUp()
    {
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(USER);
        when(globalPermissionManager.getGlobalPermission(anyString())).thenReturn(Option.none());
        when(globalPermissionManager.getGlobalPermission(any(GlobalPermissionKey.class))).thenReturn(Option.none());
        when(permissionManager.getProjectPermission(any(ProjectPermissionKey.class))).thenReturn(Option.none());
        when(projectManager.getProjectObj(Long.valueOf(PROJECT_ID))).thenReturn(PROJECT);
        when(issueManager.getIssueObject(Long.valueOf(ISSUE_ID))).thenReturn(ISSUE);
    }

    @Test
    public void globalPermissions()
    {
        assumeGlobalPermissionExistsWithValue("BUILT_IN", true);
        assertThat(resolver.resolve("", "globalPermission.BUILT_IN", emptyMap()), equalTo(Optional.of("true")));
        assertThat(resolver.resolve("", "globalPermission.built_in", emptyMap()), equalTo(Optional.of("true")));

        assumeGlobalPermissionExistsWithValue("addOnKey__addOnPermission", true);
        assertThat(resolver.resolve("addOnKey", "globalPermission.addOnPermission", emptyMap()), equalTo(Optional.of("true")));
        assertThat(resolver.resolve("addOnKey", "globalPermission.addOnKey__addOnPermission", emptyMap()), equalTo(Optional.of("true")));
        assertThat(resolver.resolve("differentAddOn", "globalPermission.addOnKey__addOnPermission", emptyMap()), equalTo(Optional.of("true")));

        assertThat(resolver.resolve("", "globalPermission.unknownPermission", emptyMap()), equalTo(Optional.empty()));
    }

    @Test
    public void projectPermissions()
    {
        assumeProjectPermissionExistsWithValue("BUILT_IN", true);
        assertThat(resolver.resolve("", "projectPermission.built_in", context().withIssue()), equalTo(Optional.of("true")));
        assertThat(resolver.resolve("", "projectPermission.built_in", context().withProject()), equalTo(Optional.of("true")));
        assertThat(resolver.resolve("", "projectPermission.built_in", context().withIssue().withProject()), equalTo(Optional.of("true")));
        assertThat(resolver.resolve("", "projectPermission.built_in", context()), equalTo(Optional.empty()));

        assumeProjectPermissionExistsWithValue("addOnKey__addOnPermission", true);

        Context context = context().withIssue().withProject();

        assertThat(resolver.resolve("addOnKey", "projectPermission.addOnPermission", context), equalTo(Optional.of("true")));
        assertThat(resolver.resolve("addOnKey", "projectPermission.addOnKey__addOnPermission", context), equalTo(Optional.of("true")));
        assertThat(resolver.resolve("differentAddOn", "projectPermission.addOnKey__addOnPermission", context), equalTo(Optional.of("true")));

        assertThat(resolver.resolve("", "projectPermission.unknownPermission", context), equalTo(Optional.empty()));
    }

    private void assumeProjectPermissionExistsWithValue(String permission, boolean permissionValueForTheCurrentUser)
    {
        ProjectPermission projectPermission = mock(ProjectPermission.class);
        ProjectPermissionKey key = new ProjectPermissionKey(permission);
        when(permissionManager.getProjectPermission(key)).thenReturn(Option.option(projectPermission));
        when(permissionManager.hasPermission(key, ISSUE, USER)).thenReturn(permissionValueForTheCurrentUser);
        when(permissionManager.hasPermission(key, PROJECT, USER)).thenReturn(permissionValueForTheCurrentUser);
    }

    private void assumeGlobalPermissionExistsWithValue(String permission, boolean permissionValueForTheCurrentUser)
    {
        GlobalPermissionType globalPermission = new GlobalPermissionType(permission, permission, permission, true);
        when(globalPermissionManager.getGlobalPermission(permission)).thenReturn(Option.option(globalPermission));
        when(globalPermissionManager.getGlobalPermission(GlobalPermissionKey.of(permission))).thenReturn(Option.option(globalPermission));
        when(globalPermissionManager.hasPermission(GlobalPermissionKey.of(permission), USER)).thenReturn(permissionValueForTheCurrentUser);
    }

    private static Context context()
    {
        return new Context();
    }

    private final static class Context extends HashMap<String, Object>
    {
        public Context withProject()
        {
            put("project.id", PROJECT_ID);
            return this;
        }

        public Context withIssue()
        {
            put("issue.id", ISSUE_ID);
            return this;
        }
    }
}
