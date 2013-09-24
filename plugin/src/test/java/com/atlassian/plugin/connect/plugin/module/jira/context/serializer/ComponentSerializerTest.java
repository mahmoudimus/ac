package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.crowd.model.user.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentService;
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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ComponentSerializerTest
{
    @Mock
    private ProjectComponentService componentService;

    @Mock
    private UserManager userManager;

    @Mock
    private User user;

    @Mock
    private ErrorCollection errorCollection;

    @Mock
    private ProjectComponent component1;

    @Test
    public void shouldReturnAbsentIfNoProjectComponentInParams() throws UnauthorisedException, ResourceNotFoundException
    {
        final ParameterDeserializer<ProjectComponent> serializer = new ComponentSerializer(componentService, userManager);
        final Optional<ProjectComponent> component = serializer.deserialize(ImmutableMap.of("blah", new Object()), "fred");
        assertThat(component.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfProjectComponentIsNotMap() throws UnauthorisedException, ResourceNotFoundException
    {
        final ParameterDeserializer<ProjectComponent> serializer = new ComponentSerializer(componentService, userManager);
        final Optional<ProjectComponent> component = serializer.deserialize(ImmutableMap.of("component", new Object()), "fred");
        assertThat(component.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfNoIdOrKeyInProjectComponent() throws UnauthorisedException, ResourceNotFoundException
    {
        final ParameterDeserializer<ProjectComponent> serializer = new ComponentSerializer(componentService, userManager);
        final Optional<ProjectComponent> component = serializer.deserialize(
                ImmutableMap.<String, Object>of("component", ImmutableMap.of("foo", new Object())),
                "fred");
        assertThat(component.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfNoUserForUsername() throws UnauthorisedException, ResourceNotFoundException
    {
        final ParameterDeserializer<ProjectComponent> serializer = new ComponentSerializer(componentService, userManager);
        final Optional<ProjectComponent> component = serializer.deserialize(
                ImmutableMap.<String, Object>of("component", ImmutableMap.of("id", 10)), "fred");

        assertThat(component.isPresent(), is(false));
        verify(userManager, times(1)).getUserByName("fred");
    }

    @Test
    public void shouldReturnAbsentIfNoProjectComponentForId() throws UnauthorisedException, ResourceNotFoundException
    {
        when(userManager.getUserByName("fred")).thenReturn(new DelegatingApplicationUser("fred", user));
        when(componentService.find(any(User.class), (ErrorCollection) eq(null), eq(10l))).thenReturn(null);
        when(errorCollection.hasAnyErrors()).thenReturn(true);

        final ParameterDeserializer<ProjectComponent> serializer = new ComponentSerializer(componentService, userManager);
        final Optional<ProjectComponent> component = serializer.deserialize(
                ImmutableMap.<String, Object>of("component", ImmutableMap.of("id", 10)), "fred");

        assertThat(component.isPresent(), is(false));
        verify(componentService, times(1)).find(any(User.class), (ErrorCollection) eq(null), eq(10l));
    }

    @Test
    public void shouldReturnProjectComponentWhenTheStarsAlign() throws UnauthorisedException, ResourceNotFoundException
    {
        when(userManager.getUserByName("fred")).thenReturn(new DelegatingApplicationUser("fred", user));
        when(componentService.find(any(User.class), (ErrorCollection) eq(null), eq(10l))).thenReturn(component1);
        when(errorCollection.hasAnyErrors()).thenReturn(false);

        final ParameterDeserializer<ProjectComponent> serializer = new ComponentSerializer(componentService, userManager);
        final Optional<ProjectComponent> component = serializer.deserialize(
                ImmutableMap.<String, Object>of("component", ImmutableMap.of("id", 10)), "fred");

        assertThat(component.isPresent(), is(true));
    }
}
