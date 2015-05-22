package com.atlassian.plugin.connect.jira.condition;

import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@Ignore("TODO: tim fix this")
@RunWith(MockitoJUnitRunner.class)
public class IsProjectAdminConditionTest
{
    private static Map<String, Object> CONTEXT = Collections.emptyMap();

    @Mock private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock private ProjectManager projectManager;
    @Mock private Project project;
    @Mock private ApplicationUser user;
    @Mock private ProjectService projectService;
    @Mock private ProjectService.GetProjectResult getProjectResult;

    @Test
    public void shouldDisplayWithAValidProjectAndUser()
    {
        when(project.getKey()).thenReturn("key");
        when(jiraAuthenticationContext.getUser()).thenReturn(user);
        when(projectService.getProjectByKeyForAction(eq(user), eq("key"), eq(ProjectAction.EDIT_PROJECT_CONFIG))).thenReturn(getProjectResult);
        when(getProjectResult.isValid()).thenReturn(true);
        IsProjectAdminCondition condition = new IsProjectAdminCondition(jiraAuthenticationContext, projectService);
        assertThat(condition.shouldDisplay(CONTEXT), is(true));
    }

    @Test
    public void shouldNotDisplayWithAnInvalidProjectAndAValidUser()
    {
        when(project.getKey()).thenReturn("key");
        when(jiraAuthenticationContext.getUser()).thenReturn(user);
        when(projectService.getProjectByKeyForAction(eq(user), eq("key"), eq(ProjectAction.EDIT_PROJECT_CONFIG))).thenReturn(getProjectResult);
        when(getProjectResult.isValid()).thenReturn(false);
        IsProjectAdminCondition condition = new IsProjectAdminCondition(jiraAuthenticationContext, projectService);
        assertThat(condition.shouldDisplay(CONTEXT), is(false));
    }

    @Test
    public void shouldNotDisplayWithNoProjectButAValidUser()
    {
        when(jiraAuthenticationContext.getUser()).thenReturn(user);
        when(projectService.getProjectByKeyForAction(eq(user), eq("key"), eq(ProjectAction.EDIT_PROJECT_CONFIG))).thenReturn(getProjectResult);
        when(getProjectResult.isValid()).thenReturn(true);
        IsProjectAdminCondition condition = new IsProjectAdminCondition(jiraAuthenticationContext, projectService);
        assertThat(condition.shouldDisplay(CONTEXT), is(false));
    }

    // just in case there is a "no project exists" project
    @Test
    public void shouldNotDisplayWithANullProjectKeyButAValidUser()
    {
        when(jiraAuthenticationContext.getUser()).thenReturn(user);
        when(projectService.getProjectByKeyForAction(eq(user), eq("key"), eq(ProjectAction.EDIT_PROJECT_CONFIG))).thenReturn(getProjectResult);
        when(getProjectResult.isValid()).thenReturn(true);
        IsProjectAdminCondition condition = new IsProjectAdminCondition(jiraAuthenticationContext, projectService);
        assertThat(condition.shouldDisplay(CONTEXT), is(false));
    }

    @Test
    public void shouldNotDisplayWithAValidUserWhenTheProjectServiceReturnsNull()
    {
        when(project.getKey()).thenReturn("key");
        when(jiraAuthenticationContext.getUser()).thenReturn(user);
        when(projectService.getProjectByKeyForAction(eq(user), eq("key"), eq(ProjectAction.EDIT_PROJECT_CONFIG))).thenReturn(null);
        when(getProjectResult.isValid()).thenReturn(true);
        IsProjectAdminCondition condition = new IsProjectAdminCondition(jiraAuthenticationContext, projectService);
        assertThat(condition.shouldDisplay(CONTEXT), is(false));
    }

    @Test
    public void shouldNotDisplayWithAValidProjectButNoUser()
    {
        when(project.getKey()).thenReturn("key");
        when(projectService.getProjectByKeyForAction(eq(user), eq("key"), eq(ProjectAction.EDIT_PROJECT_CONFIG))).thenReturn(getProjectResult);
        when(getProjectResult.isValid()).thenReturn(true);
        IsProjectAdminCondition condition = new IsProjectAdminCondition(jiraAuthenticationContext, projectService);
        assertThat(condition.shouldDisplay(CONTEXT), is(false));
    }

    @Before
    public void beforeEachTest()
    {
        ComponentAccessor.initialiseWorker(new ComponentAccessor.Worker()
        {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getComponent(Class<T> componentClass)
            {
                return (T) projectService;
            }

            @Override
            public <T> T getComponentOfType(Class<T> componentClass)
            {
                return null;
            }

            @Override
            public <T> T getOSGiComponentInstanceOfType(Class<T> componentClass)
            {
                return null;
            }
        });
    }
}
