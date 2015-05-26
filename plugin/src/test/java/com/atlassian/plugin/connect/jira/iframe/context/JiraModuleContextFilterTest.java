package com.atlassian.plugin.connect.jira.iframe.context;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.spi.iframe.context.HashMapModuleContextParameters;
import com.atlassian.plugin.connect.api.iframe.context.ModuleContextParameters;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static com.atlassian.plugin.connect.jira.iframe.context.JiraModuleContextFilter.COMPONENT_ID;
import static com.atlassian.plugin.connect.jira.iframe.context.JiraModuleContextFilter.ISSUE_ID;
import static com.atlassian.plugin.connect.jira.iframe.context.JiraModuleContextFilter.ISSUE_KEY;
import static com.atlassian.plugin.connect.jira.iframe.context.JiraModuleContextFilter.POSTFUNCTION_CONFIG;
import static com.atlassian.plugin.connect.jira.iframe.context.JiraModuleContextFilter.POSTFUNCTION_ID;
import static com.atlassian.plugin.connect.jira.iframe.context.JiraModuleContextFilter.PROJECT_ID;
import static com.atlassian.plugin.connect.jira.iframe.context.JiraModuleContextFilter.PROJECT_KEY;
import static com.atlassian.plugin.connect.jira.iframe.context.JiraModuleContextFilter.VERSION_ID;
import static com.atlassian.plugin.connect.spi.iframe.context.AbstractModuleContextFilter.PROFILE_KEY;
import static com.atlassian.plugin.connect.spi.iframe.context.AbstractModuleContextFilter.PROFILE_NAME;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class JiraModuleContextFilterTest
{
    @Mock
    private PermissionManager permissionManager;
    @Mock
    private ProjectService projectService;
    @Mock
    private IssueManager issueManager;
    @Mock
    private VersionManager versionManager;
    @Mock
    private ProjectComponentManager projectComponentManager;
    @Mock
    private JiraAuthenticationContext authenticationContext;

    @Mock
    private PluginAccessor pluginAccessor;

    @InjectMocks
    private JiraModuleContextFilter jiraModuleContextFilter;

    @Mock
    private ProjectService.GetProjectResult validResult;
    @Mock
    private ProjectService.GetProjectResult invalidResult;

    @Mock
    private MutableIssue allowedIssue;
    @Mock
    private MutableIssue forbiddenIssue;

    private final static String ALLOWED_ISSUE_ID = "101";
    private final static String FORBIDDEN_ISSUE_ID = "202";

    private final static String ALLOWED_ISSUE_KEY = "GOOD-1";
    private final static String FORBIDDEN_ISSUE_KEY = "BAD-1";

    @Mock
    private Project allowedProject;
    @Mock
    private Project forbiddenProject;

    private final static String ALLOWED_PROJECT_ID = "303";
    private final static String FORBIDDEN_PROJECT_ID = "404";

    private final static String ALLOWED_PROJECT_KEY = "GOOD";
    private final static String FORBIDDEN_PROJECT_KEY = "BAD";

    @Mock
    private Version allowedVersion;
    @Mock
    private Version forbiddenVersion;

    private final static String ALLOWED_VERSION_ID = "10001";
    private final static String FORBIDDEN_VERSION_ID = "20002";

    @Mock
    private ProjectComponent allowedComponent;
    @Mock
    private ProjectComponent forbiddenComponent;

    private final static String ALLOWED_COMPONENT_ID = "777";
    private final static String FORBIDDEN_COMPONENT_ID = "888";

    @Before
    public void setUp() throws Exception
    {
        when(authenticationContext.getUser()).thenReturn(mock(ApplicationUser.class));

        when(validResult.isValid()).thenReturn(true);
        when(invalidResult.isValid()).thenReturn(false);

        when(issueManager.getIssueObject(eq(Long.parseLong(ALLOWED_ISSUE_ID)))).thenReturn(allowedIssue);
        when(issueManager.getIssueObject(eq(ALLOWED_ISSUE_KEY))).thenReturn(allowedIssue);
        when(issueManager.getIssueObject(eq(Long.parseLong(FORBIDDEN_ISSUE_ID)))).thenReturn(forbiddenIssue);
        when(issueManager.getIssueObject(eq(FORBIDDEN_ISSUE_KEY))).thenReturn(forbiddenIssue);

        when(permissionManager.hasPermission(anyInt(), eq(allowedIssue), any(ApplicationUser.class))).thenReturn(true);
        when(permissionManager.hasPermission(anyInt(), eq(forbiddenIssue), any(ApplicationUser.class))).thenReturn(false);

        when(allowedVersion.getProjectObject()).thenReturn(allowedProject);
        when(forbiddenVersion.getProjectObject()).thenReturn(forbiddenProject);

        when(versionManager.getVersion(eq(Long.parseLong(ALLOWED_VERSION_ID)))).thenReturn(allowedVersion);
        when(versionManager.getVersion(eq(Long.parseLong(FORBIDDEN_VERSION_ID)))).thenReturn(forbiddenVersion);

        when(allowedComponent.getProjectId()).thenReturn(Long.parseLong(ALLOWED_PROJECT_ID));
        when(forbiddenComponent.getProjectId()).thenReturn(Long.parseLong(FORBIDDEN_PROJECT_ID));

        when(projectComponentManager.find(eq(Long.valueOf(ALLOWED_COMPONENT_ID)))).thenReturn(allowedComponent);
        when(projectComponentManager.find(eq(Long.valueOf(FORBIDDEN_COMPONENT_ID)))).thenReturn(forbiddenComponent);

        when(permissionManager.hasPermission(anyInt(), eq(allowedProject), any(ApplicationUser.class))).thenReturn(true);
        when(permissionManager.hasPermission(anyInt(), eq(forbiddenProject), any(ApplicationUser.class))).thenReturn(false);

        when(projectService.getProjectById(any(ApplicationUser.class), eq(Long.parseLong(ALLOWED_PROJECT_ID)))).thenReturn(validResult);
        when(projectService.getProjectByKey(any(ApplicationUser.class), eq(ALLOWED_PROJECT_KEY))).thenReturn(validResult);
        when(projectService.getProjectById(any(ApplicationUser.class), eq(Long.parseLong(FORBIDDEN_PROJECT_ID)))).thenReturn(invalidResult);
        when(projectService.getProjectByKey(any(ApplicationUser.class), eq(FORBIDDEN_PROJECT_KEY))).thenReturn(invalidResult);
    }

    private void testFilter(Map<String, String> input, Map<String, String> expectedOutput)
    {
        ModuleContextParameters unfiltered = new HashMapModuleContextParameters();
        unfiltered.putAll(input);

        ModuleContextParameters filtered = jiraModuleContextFilter.filter(unfiltered);

        if (expectedOutput.isEmpty())
        {
            assertTrue("Filtered context should be empty", filtered.isEmpty());
        }
        else
        {
            for (Map.Entry<String, String> expectedEntry : expectedOutput.entrySet())
            {
                assertThat(filtered, hasEntry(expectedEntry.getKey(), expectedEntry.getValue()));
            }
            for (Map.Entry<String, String> unexpectedEntry : Maps.difference(input, expectedOutput).entriesOnlyOnLeft().entrySet())
            {
                assertThat(filtered, not(hasEntry(unexpectedEntry.getKey(), unexpectedEntry.getValue())));
            }
            assertThat("Filtered context is the wrong size", filtered.size(), is(expectedOutput.size()));
        }
    }

    private void testFilter(Map<String, String> inputAndExpectedOutput)
    {
        testFilter(inputAndExpectedOutput, inputAndExpectedOutput);
    }

    private void testFilteredOut(Map<String, String> input)
    {
        testFilter(input, Collections.<String, String>emptyMap());
    }

    @Test
    public void testAllowedIssueIdAndKey()
    {
        testFilter(ImmutableMap.of(
                ISSUE_KEY, ALLOWED_ISSUE_KEY,
                ISSUE_ID, ALLOWED_ISSUE_ID
        ));
    }

    @Test
    public void testAllowedIssueIdButForbiddenKey()
    {
        testFilter(ImmutableMap.of(
                ISSUE_KEY, FORBIDDEN_ISSUE_KEY,
                ISSUE_ID, ALLOWED_ISSUE_ID
        ), ImmutableMap.of(
                ISSUE_ID, ALLOWED_ISSUE_ID
        ));
    }

    @Test
    public void testForbiddenIssueIdAndKey()
    {
        testFilteredOut(ImmutableMap.of(
                ISSUE_KEY, FORBIDDEN_ISSUE_KEY,
                ISSUE_ID, FORBIDDEN_ISSUE_ID
        ));
    }

    @Test
    public void testAllowedProjectIdAndKey()
    {
        testFilter(ImmutableMap.of(
                PROJECT_KEY, ALLOWED_PROJECT_KEY,
                PROJECT_ID, ALLOWED_PROJECT_ID
        ));
    }

    @Test
    public void testAllowedProjectIdButForbiddenKey()
    {
        testFilter(ImmutableMap.of(
                PROJECT_KEY, FORBIDDEN_PROJECT_KEY,
                PROJECT_ID, ALLOWED_PROJECT_ID
        ), ImmutableMap.of(
                PROJECT_ID, ALLOWED_PROJECT_ID
        ));
    }

    @Test
    public void testForbiddenProjectIdAndKey()
    {
        testFilteredOut(ImmutableMap.of(
                PROJECT_KEY, FORBIDDEN_PROJECT_KEY,
                PROJECT_ID, FORBIDDEN_PROJECT_ID
        ));
    }

    @Test
    public void testAllowedVersion()
    {
        testFilter(ImmutableMap.of(VERSION_ID, ALLOWED_VERSION_ID));
    }

    @Test
    public void testForbiddenVersion()
    {
        testFilteredOut(ImmutableMap.of(VERSION_ID, FORBIDDEN_VERSION_ID));
    }

    @Test
    public void testAllowedComponent()
    {
        testFilter(ImmutableMap.of(COMPONENT_ID, ALLOWED_COMPONENT_ID));
    }

    @Test
    public void testForbiddenComponent()
    {
        testFilteredOut(ImmutableMap.of(COMPONENT_ID, FORBIDDEN_COMPONENT_ID));
    }

    @Test
    public void testProfileNameAndKeyAllowedWhenLoggedIn()
    {
        testFilter(ImmutableMap.of(PROFILE_KEY, "key master", PROFILE_NAME, "gate keeper"));
    }

    @Test
    public void testProfileNameAndKeyForbiddenWhenLoggedOut()
    {
        when(authenticationContext.getUser()).thenReturn(null);
        testFilteredOut(ImmutableMap.of(PROFILE_KEY, "key master", PROFILE_NAME, "gate keeper"));
    }

    @Test
    public void testPostFunctionIsAlwaysAllowed()
    {
        when(authenticationContext.getUser()).thenReturn(null);
        testFilter(ImmutableMap.of(POSTFUNCTION_ID, "ego", POSTFUNCTION_CONFIG, "alter-ego"));
    }

}
