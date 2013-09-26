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

    // Just sunny day here. Negative tests in IssueSerializerTest
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
