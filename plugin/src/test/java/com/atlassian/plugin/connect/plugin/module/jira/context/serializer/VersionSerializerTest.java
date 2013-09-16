package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.crowd.model.user.User;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.plugin.connect.plugin.module.context.ParameterDeserializer;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.bc.project.version.VersionService.VersionResult;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VersionSerializerTest
{
    @Mock
    private VersionService versionService;

    @Mock
    private UserManager userManager;

    @Mock
    private User user;

    @Mock
    private ErrorCollection errorCollection;

    @Mock
    private Version version1;

    @Test
    public void shouldReturnAbsentIfNoVersionInParams()
    {
        final ParameterDeserializer<Version> serializer = new VersionSerializer(versionService, userManager);
        final Optional<Version> version = serializer.deserialize(ImmutableMap.of("blah", new Object()), "fred");
        assertThat(version.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfVersionIsNotMap()
    {
        final ParameterDeserializer<Version> serializer = new VersionSerializer(versionService, userManager);
        final Optional<Version> version = serializer.deserialize(ImmutableMap.of("version", new Object()), "fred");
        assertThat(version.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfNoIdOrKeyInVersion()
    {
        final ParameterDeserializer<Version> serializer = new VersionSerializer(versionService, userManager);
        final Optional<Version> version = serializer.deserialize(
                ImmutableMap.<String, Object>of("version", ImmutableMap.of("foo", new Object())),
                "fred");
        assertThat(version.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfNoUserForUsername()
    {
        final ParameterDeserializer<Version> serializer = new VersionSerializer(versionService, userManager);
        final Optional<Version> version = serializer.deserialize(
                ImmutableMap.<String, Object>of("version", ImmutableMap.of("id", 10)), "fred");

        assertThat(version.isPresent(), is(false));
        verify(userManager, times(1)).getUserByName("fred");
    }

    @Test
    public void shouldReturnAbsentIfNoVersionForId()
    {
        when(userManager.getUserByName("fred")).thenReturn(new DelegatingApplicationUser("fred", user));
        when(versionService.getVersionById(any(User.class), eq(10l))).thenReturn(new VersionResult(errorCollection, null));
        when(errorCollection.hasAnyErrors()).thenReturn(true);

        final ParameterDeserializer<Version> serializer = new VersionSerializer(versionService, userManager);
        final Optional<Version> version = serializer.deserialize(
                ImmutableMap.<String, Object>of("version", ImmutableMap.of("id", 10)), "fred");

        assertThat(version.isPresent(), is(false));
        verify(versionService, times(1)).getVersionById(any(User.class), eq(10l));
    }

    @Test
    public void shouldReturnVersionWhenTheStarsAlign()
    {
        when(userManager.getUserByName("fred")).thenReturn(new DelegatingApplicationUser("fred", user));
        when(versionService.getVersionById(any(User.class), eq(10l))).thenReturn(new VersionResult(errorCollection, version1));
        when(errorCollection.hasAnyErrors()).thenReturn(false);

        final ParameterDeserializer<Version> serializer = new VersionSerializer(versionService, userManager);
        final Optional<Version> version = serializer.deserialize(
                ImmutableMap.<String, Object>of("version", ImmutableMap.of("id", 10)), "fred");

        assertThat(version.isPresent(), is(true));
    }
}
