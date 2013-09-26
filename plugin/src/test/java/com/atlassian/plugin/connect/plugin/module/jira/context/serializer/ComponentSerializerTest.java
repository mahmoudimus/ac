package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.crowd.model.user.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentService;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.plugin.connect.plugin.module.context.MalformedRequestException;
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

    // Just sunny day here. Negative tests in IssueSerializerTest
    @Test
    public void shouldReturnProjectComponentWhenTheStarsAlign() throws UnauthorisedException, ResourceNotFoundException, MalformedRequestException
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
