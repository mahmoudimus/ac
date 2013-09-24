package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.crowd.model.user.User;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.plugin.connect.plugin.module.context.ParameterDeserializer;
import com.atlassian.plugin.connect.plugin.module.context.ResourceNotFoundException;
import com.atlassian.plugin.connect.plugin.module.permission.UnauthorisedException;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.bc.project.ProjectService.GetProjectResult;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProjectSerializerTest
{
    @Mock
    private ProjectService projectService;

    @Mock
    private UserManager userManager;

    @Mock
    private User user;

    @Mock
    private ErrorCollection errorCollection;

    @Mock
    private Project project1;

    @Test
    public void shouldReturnAbsentIfNoProjectInParams() throws UnauthorisedException, ResourceNotFoundException
    {
        final ParameterDeserializer<Project> serializer = new ProjectSerializer(projectService, userManager);
        final Optional<Project> project = serializer.deserialize(ImmutableMap.of("blah", new Object()), "fred");
        assertThat(project.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfProjectIsNotMap() throws UnauthorisedException, ResourceNotFoundException
    {
        final ParameterDeserializer<Project> serializer = new ProjectSerializer(projectService, userManager);
        final Optional<Project> project = serializer.deserialize(ImmutableMap.of("project", new Object()), "fred");
        assertThat(project.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfNoIdOrKeyInProject() throws UnauthorisedException, ResourceNotFoundException
    {
        final ParameterDeserializer<Project> serializer = new ProjectSerializer(projectService, userManager);
        final Optional<Project> project = serializer.deserialize(
                ImmutableMap.<String, Object>of("project", ImmutableMap.of("foo", new Object())),
                "fred");
        assertThat(project.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfNoUserForUsername() throws UnauthorisedException, ResourceNotFoundException
    {
        final ParameterDeserializer<Project> serializer = new ProjectSerializer(projectService, userManager);
        final Optional<Project> project = serializer.deserialize(
                ImmutableMap.<String, Object>of("project", ImmutableMap.of("id", 10)), "fred");

        assertThat(project.isPresent(), is(false));
        verify(userManager, times(1)).getUserByName("fred");
    }

    @Test
    public void shouldReturnAbsentIfNoProjectForId() throws UnauthorisedException, ResourceNotFoundException
    {
        when(userManager.getUserByName("fred")).thenReturn(new DelegatingApplicationUser("fred", user));
        when(projectService.getProjectById(any(User.class), eq(10l))).thenReturn(new GetProjectResult(errorCollection, null));
        when(errorCollection.hasAnyErrors()).thenReturn(true);

        final ParameterDeserializer<Project> serializer = new ProjectSerializer(projectService, userManager);
        final Optional<Project> project = serializer.deserialize(
                ImmutableMap.<String, Object>of("project", ImmutableMap.of("id", 10)), "fred");

        assertThat(project.isPresent(), is(false));
        verify(projectService, times(1)).getProjectById(any(User.class), eq(10l));
    }

    @Test
    public void shouldReturnAbsentIfNoProjectForKey() throws UnauthorisedException, ResourceNotFoundException
    {
        when(userManager.getUserByName("fred")).thenReturn(new DelegatingApplicationUser("fred", user));
        when(projectService.getProjectByKey(any(User.class), eq("myKey"))).thenReturn(new GetProjectResult(errorCollection, null));
        when(errorCollection.hasAnyErrors()).thenReturn(true);

        final ParameterDeserializer<Project> serializer = new ProjectSerializer(projectService, userManager);
        final Optional<Project> project = serializer.deserialize(
                ImmutableMap.<String, Object>of("project", ImmutableMap.of("key", "myKey")), "fred");

        assertThat(project.isPresent(), is(false));
        verify(projectService, times(1)).getProjectByKey(any(User.class), eq("myKey"));
    }

    @Test
    public void shouldReturnProjectWhenTheStarsAlign() throws UnauthorisedException, ResourceNotFoundException
    {
        when(userManager.getUserByName("fred")).thenReturn(new DelegatingApplicationUser("fred", user));
        when(projectService.getProjectByKey(any(User.class), eq("myKey"))).thenReturn(new GetProjectResult(errorCollection, project1));
        when(errorCollection.hasAnyErrors()).thenReturn(false);

        final ParameterDeserializer<Project> serializer = new ProjectSerializer(projectService, userManager);
        final Optional<Project> project = serializer.deserialize(
                ImmutableMap.<String, Object>of("project", ImmutableMap.of("key", "myKey")), "fred");

        assertThat(project.isPresent(), is(true));
    }
}
