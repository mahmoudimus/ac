package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.crowd.model.user.User;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.project.version.Version;
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

    // Just sunny day here. Negative tests in IssueSerializerTest
    @Test
    public void shouldReturnVersionWhenTheStarsAlign() throws UnauthorisedException, ResourceNotFoundException
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
