package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.plugin.module.context.MalformedRequestException;
import com.atlassian.plugin.connect.plugin.module.context.ParameterDeserializer;
import com.atlassian.plugin.connect.plugin.module.context.ResourceNotFoundException;
import com.atlassian.plugin.connect.plugin.module.permission.UnauthorisedException;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.security.Permissions.USER_PICKER;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProfileUserSerializerTest
{
    @Mock
    private UserManager userManager;

    @Mock
    private User user;

    @Mock
    private User profileUser;

    @Mock
    private PermissionManager permissionManager;

    @Test
    public void shouldReturnUserByName() throws UnauthorisedException, ResourceNotFoundException, MalformedRequestException
    {
        when(userManager.getUserByName("fred")).thenReturn(new DelegatingApplicationUser("fred", user));
        when(user.getName()).thenReturn("fred");
        when(userManager.getUserByName("barney")).thenReturn(new DelegatingApplicationUser("barney", profileUser));
        when(permissionManager.hasPermission(eq(USER_PICKER), any(User.class))).thenReturn(true);

        final ParameterDeserializer<ApplicationUser> serializer = new ProfileUserSerializer(permissionManager, userManager);
        final Optional<ApplicationUser> lookedupUser = serializer.deserialize(
                ImmutableMap.<String, Object>of("profileUser", ImmutableMap.of("name", "barney")), "fred");

        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(permissionManager, times(1)).hasPermission(eq(USER_PICKER), userCaptor.capture());
        assertThat(userCaptor.getValue().getName(), is(equalTo("fred")));
        assertThat(lookedupUser.isPresent(), is(true));
    }

    @Test
    public void shouldReturnUserByKey() throws UnauthorisedException, ResourceNotFoundException, MalformedRequestException
    {
        when(userManager.getUserByName("fred")).thenReturn(new DelegatingApplicationUser("fred", user));
        when(user.getName()).thenReturn("fred");
        when(userManager.getUserByKey("barney")).thenReturn(new DelegatingApplicationUser("barney", profileUser));
        when(permissionManager.hasPermission(eq(USER_PICKER), any(User.class))).thenReturn(true);

        final ParameterDeserializer<ApplicationUser> serializer = new ProfileUserSerializer(permissionManager, userManager);
        final Optional<ApplicationUser> lookedupUser = serializer.deserialize(
                ImmutableMap.<String, Object>of("profileUser", ImmutableMap.of("key", "barney")), "fred");

        assertThat(lookedupUser.isPresent(), is(true));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldThrowResourceNotFoundWhenUserDoesNotHavePermission() throws ResourceNotFoundException, UnauthorisedException, MalformedRequestException
    {
        when(userManager.getUserByName("fred")).thenReturn(new DelegatingApplicationUser("fred", user));
        when(user.getName()).thenReturn("fred");
        when(userManager.getUserByKey("barney")).thenReturn(new DelegatingApplicationUser("barney", profileUser));
        when(permissionManager.hasPermission(eq(USER_PICKER), any(User.class))).thenReturn(false);

        final ParameterDeserializer<ApplicationUser> serializer = new ProfileUserSerializer(permissionManager, userManager);
        serializer.deserialize(
                ImmutableMap.<String, Object>of("profileUser", ImmutableMap.of("key", "barney")), "fred");

    }
}
