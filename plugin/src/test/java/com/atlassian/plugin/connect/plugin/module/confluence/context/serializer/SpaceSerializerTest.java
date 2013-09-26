package com.atlassian.plugin.connect.plugin.module.confluence.context.serializer;

import com.atlassian.confluence.content.service.SpaceService;
import com.atlassian.confluence.content.service.space.KeySpaceLocator;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.plugin.connect.plugin.module.context.ParameterDeserializer;
import com.atlassian.plugin.connect.plugin.module.context.ResourceNotFoundException;
import com.atlassian.plugin.connect.plugin.module.permission.UnauthorisedException;
import com.atlassian.user.EntityException;
import com.atlassian.user.User;
import com.atlassian.user.UserManager;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@Ignore // TODO: Apply changes that were applied to IssueSerializerTest
@RunWith(MockitoJUnitRunner.class)
public class SpaceSerializerTest
{
    @Mock
    private SpaceService spaceService;

    @Mock
    private UserManager userManager;

    @Mock
    private User user;

//    @Mock
//    private ErrorCollection errorCollection;

    @Mock
    private Space space1;

    @Mock
    private SpaceManager spaceManager;

    @Mock
    private PermissionManager permissionManager;

    @Test
    public void shouldReturnAbsentIfNoSpaceInParams() throws ResourceNotFoundException, UnauthorisedException
    {
        final ParameterDeserializer<Space> serializer = new SpaceSerializer(spaceService, userManager, permissionManager);
        final Optional<Space> space = serializer.deserialize(ImmutableMap.of("blah", new Object()), "fred");
        assertThat(space.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfSpaceIsNotMap() throws ResourceNotFoundException, UnauthorisedException
    {
        final ParameterDeserializer<Space> serializer = new SpaceSerializer(spaceService, userManager, permissionManager);
        final Optional<Space> space = serializer.deserialize(ImmutableMap.of("space", new Object()), "fred");
        assertThat(space.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfNoIdOrKeyInSpace() throws ResourceNotFoundException, UnauthorisedException
    {
        final ParameterDeserializer<Space> serializer = new SpaceSerializer(spaceService, userManager, permissionManager);
        final Optional<Space> space = serializer.deserialize(
                ImmutableMap.<String, Object>of("space", ImmutableMap.of("foo", new Object())),
                "fred");
        assertThat(space.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfNoUserForUsername() throws EntityException, ResourceNotFoundException, UnauthorisedException
    {
        final ParameterDeserializer<Space> serializer = new SpaceSerializer(spaceService, userManager, permissionManager);
        final Optional<Space> space = serializer.deserialize(
                ImmutableMap.<String, Object>of("space", ImmutableMap.of(
                        "id", 10,
                        "key", "mykey")),
                "fred");

        assertThat(space.isPresent(), is(false));
        verify(userManager, times(1)).getUser("fred");
    }

    @Test
    public void shouldReturnAbsentIfNoSpaceForKey() throws EntityException, ResourceNotFoundException, UnauthorisedException
    {
        when(userManager.getUser("fred")).thenReturn(user);
        when(spaceService.getKeySpaceLocator("mykey")).thenReturn(new KeySpaceLocator(spaceManager, "mykey"));
        when(spaceManager.getSpace("mykey")).thenReturn(null);

        final ParameterDeserializer<Space> serializer = new SpaceSerializer(spaceService, userManager, permissionManager);
        final Optional<Space> space = serializer.deserialize(
                ImmutableMap.<String, Object>of("space", ImmutableMap.of("key", "mykey")), "fred");

        assertThat(space.isPresent(), is(false));
        verify(spaceService, times(1)).getKeySpaceLocator("mykey");
    }

    @Test
    public void shouldReturnSpaceWhenTheStarsAlign() throws EntityException, ResourceNotFoundException, UnauthorisedException
    {
        when(userManager.getUser("fred")).thenReturn(user);
        when(spaceService.getKeySpaceLocator("mykey")).thenReturn(new KeySpaceLocator(spaceManager, "mykey"));
        when(spaceManager.getSpace("mykey")).thenReturn(space1);

        final ParameterDeserializer<Space> serializer = new SpaceSerializer(spaceService, userManager, permissionManager);
        final Optional<Space> space = serializer.deserialize(
                ImmutableMap.<String, Object>of("space", ImmutableMap.of("key", "mykey")), "fred");

        assertThat(space.isPresent(), is(true));
    }
}
