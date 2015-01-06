package com.atlassian.plugin.connect.plugin.iframe.servlet.context;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextParametersImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager.CONTEXT_KEY_HELPER;
import static com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager.CONTEXT_KEY_USER;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class JiraContextFactoryTest
{

    @Mock
    private JiraAuthenticationContext authenticationContext;

    @Mock
    private IssueManager issueManager;

    @Mock
    private ProjectService projectService;

    private JiraContextFactory jiraContextFactory;


    @Before
    public void setUp() throws Exception
    {
        jiraContextFactory = new JiraContextFactory(authenticationContext, issueManager, projectService);
    }

    @Test
    public void testReturningJiraContextWhenAllParametersAreDefined()
    {
        final ApplicationUser user = mock(ApplicationUser.class);
        when(authenticationContext.getUser()).thenReturn(user);

        final String projectKey = "project key";
        final Project project = mock(Project.class);
        final ProjectService.GetProjectResult projectResult = mock(ProjectService.GetProjectResult.class);
        when(projectResult.getProject()).thenReturn(project);
        when(projectService.getProjectByKey(user, projectKey)).thenReturn(projectResult);

        final String issueKey = "issue key";
        final MutableIssue issue = mock(MutableIssue.class);
        when(issueManager.getIssueObject(issueKey)).thenReturn(issue);

        final ModuleContextParameters parameters = mock(ModuleContextParameters.class);
        when(parameters.get(JiraModuleContextFilter.ISSUE_KEY)).thenReturn(issueKey);
        when(parameters.get(JiraModuleContextFilter.PROJECT_KEY)).thenReturn(projectKey);

        final Map<String, Object> productSpecificContext = jiraContextFactory.createProductSpecificContext(parameters);
        final JiraHelper returnedJiraHelper = (JiraHelper) productSpecificContext.get(CONTEXT_KEY_HELPER);
        final ApplicationUser returnedUser = (ApplicationUser) productSpecificContext.get(CONTEXT_KEY_USER);
        final Map<String, Object> expectedHelperParams = MapBuilder.<String, Object>newBuilder()
                .add("request", null).add("project", project).add("issue", issue).toMutableMap();

        assertThat(returnedJiraHelper.getProjectObject(), is(project));
        assertThat(returnedJiraHelper.getContextParams(), is(expectedHelperParams));
        assertThat(returnedUser, is(user));
    }

    @Test
    public void testWhenUserNotLogged() throws Exception
    {
        when(authenticationContext.getUser()).thenReturn(null);

        ModuleContextParameters emptyModuleParams = new JiraModuleContextParametersImpl();
        final Map<String, Object> productSpecificContext = jiraContextFactory.createProductSpecificContext(emptyModuleParams);

        assertThat(productSpecificContext, is(Collections.<String, Object>emptyMap()));
    }

    @Test
    public void testWhenIsNoValidIssueInModuleContext() throws Exception
    {
        final ApplicationUser user = mock(ApplicationUser.class);
        when(authenticationContext.getUser()).thenReturn(user);

        final String projectKey = "project key";
        final Project project = mock(Project.class);
        final ProjectService.GetProjectResult projectResult = mock(ProjectService.GetProjectResult.class);
        when(projectResult.getProject()).thenReturn(project);
        when(projectService.getProjectByKey(user, projectKey)).thenReturn(projectResult);

        final String issueKey = "issue key";
        when(issueManager.getIssueObject(issueKey)).thenReturn(null);

        final ModuleContextParameters parameters = mock(ModuleContextParameters.class);
        when(parameters.get(JiraModuleContextFilter.ISSUE_KEY)).thenReturn(issueKey);
        when(parameters.get(JiraModuleContextFilter.PROJECT_KEY)).thenReturn(projectKey);

        final Map<String, Object> productSpecificContext = jiraContextFactory.createProductSpecificContext(parameters);
        final JiraHelper returnedJiraHelper = (JiraHelper) productSpecificContext.get(CONTEXT_KEY_HELPER);
        final ApplicationUser returnedUser = (ApplicationUser) productSpecificContext.get(CONTEXT_KEY_USER);
        final Map<String, Object> expectedHelperParams = MapBuilder.<String, Object>newBuilder()
                .add("request", null).add("project", project).toMutableMap();

        assertThat(returnedJiraHelper.getProjectObject(), is(project));
        assertThat(returnedJiraHelper.getContextParams(), is(expectedHelperParams));
        assertThat(returnedUser, is(user));
    }
}