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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.confluence.security.Permission.VIEW;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpaceSerializerTest
{
    @Mock
    private SpaceService spaceService;

    @Mock
    private UserManager userManager;

    @Mock
    private User user;

    @Mock
    private Space space1;

    @Mock
    private SpaceManager spaceManager;

    @Mock
    private PermissionManager permissionManager;

    // Just sunny day here. Negative tests in PageSerializerTest

    @Test
    public void shouldReturnSpaceWhenTheStarsAlign() throws EntityException, ResourceNotFoundException, UnauthorisedException
    {
        when(userManager.getUser("fred")).thenReturn(user);
        when(spaceService.getKeySpaceLocator("mykey")).thenReturn(new KeySpaceLocator(spaceManager, "mykey"));
        when(spaceManager.getSpace("mykey")).thenReturn(space1);
        when(permissionManager.hasPermission(user, VIEW, space1)).thenReturn(true);

        final ParameterDeserializer<Space> serializer = new SpaceSerializer(spaceService, userManager, permissionManager);
        final Optional<Space> space = serializer.deserialize(
                ImmutableMap.<String, Object>of("space", ImmutableMap.of("key", "mykey")), "fred");

        assertThat(space.isPresent(), is(true));
    }
}
