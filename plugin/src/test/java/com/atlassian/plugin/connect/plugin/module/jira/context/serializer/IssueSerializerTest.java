package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.crowd.model.user.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IssueSerializerTest
{
    @Mock
    private IssueService issueService;

    @Mock
    private UserManager userManager;

    @Mock
    private User user;

    @Mock
    private ErrorCollection errorCollection;

    @Mock
    private MutableIssue issue1;

    @Test
    public void shouldReturnAbsentIfNoIssueInParams() throws UnauthorisedException, ResourceNotFoundException
    {
        final ParameterDeserializer<Issue> serializer = new IssueSerializer(issueService, userManager);
        final Optional<Issue> issue = serializer.deserialize(ImmutableMap.of("blah", new Object()), "fred");
        assertThat(issue.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfIssueIsNotMap() throws UnauthorisedException, ResourceNotFoundException
    {
        fail("this should throw an error");
        final ParameterDeserializer<Issue> serializer = new IssueSerializer(issueService, userManager);
        final Optional<Issue> issue = serializer.deserialize(ImmutableMap.of("issue", new Object()), "fred");
        assertThat(issue.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfNoIdOrKeyInIssue() throws UnauthorisedException, ResourceNotFoundException
    {
        fail("this should throw an error - ResourceNotFoundException???");

        final ParameterDeserializer<Issue> serializer = new IssueSerializer(issueService, userManager);
        final Optional<Issue> issue = serializer.deserialize(
                ImmutableMap.<String, Object>of("issue", ImmutableMap.of("foo", new Object())),
                "fred");
        assertThat(issue.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfNoUserForUsername() throws UnauthorisedException, ResourceNotFoundException
    {
        fail("this should throw an error. Must not give out details of if user exits. " +
                "Maybe either ResourceNotFound or Unauthorised depending on policy of product");

        final ParameterDeserializer<Issue> serializer = new IssueSerializer(issueService, userManager);
        final Optional<Issue> issue = serializer.deserialize(
                ImmutableMap.<String, Object>of("issue", ImmutableMap.of("id", 10)), "fred");

        assertThat(issue.isPresent(), is(false));
        verify(userManager, times(1)).getUserByName("fred");
    }

    @Test
    public void shouldReturnAbsentIfNoIssueForId() throws UnauthorisedException, ResourceNotFoundException
    {
        fail("this should throw an error - ResourceNotFoundException");
        when(userManager.getUserByName("fred")).thenReturn(new DelegatingApplicationUser("fred", user));
        when(issueService.getIssue(any(User.class), eq(10l))).thenReturn(new IssueService.IssueResult(null, errorCollection));
        when(errorCollection.hasAnyErrors()).thenReturn(true);

        final ParameterDeserializer<Issue> serializer = new IssueSerializer(issueService, userManager);
        final Optional<Issue> issue = serializer.deserialize(
                ImmutableMap.<String, Object>of("issue", ImmutableMap.of("id", 10)), "fred");

        assertThat(issue.isPresent(), is(false));
        verify(issueService, times(1)).getIssue(any(User.class), eq(10l));
    }

    @Test
    public void shouldReturnAbsentIfNoIssueForKey() throws UnauthorisedException, ResourceNotFoundException
    {
        fail("this should throw an error - ResourceNotFoundException");
        when(userManager.getUserByName("fred")).thenReturn(new DelegatingApplicationUser("fred", user));
        when(issueService.getIssue(any(User.class), eq("myKey"))).thenReturn(new IssueService.IssueResult(null, errorCollection));
        when(errorCollection.hasAnyErrors()).thenReturn(true);

        final ParameterDeserializer<Issue> serializer = new IssueSerializer(issueService, userManager);
        final Optional<Issue> issue = serializer.deserialize(
                ImmutableMap.<String, Object>of("issue", ImmutableMap.of("key", "myKey")), "fred");

        assertThat(issue.isPresent(), is(false));
        verify(issueService, times(1)).getIssue(any(User.class), eq("myKey"));
    }

    @Test
    public void shouldReturnIssueWhenTheStarsAlign() throws UnauthorisedException, ResourceNotFoundException
    {
        when(userManager.getUserByName("fred")).thenReturn(new DelegatingApplicationUser("fred", user));
        when(issueService.getIssue(any(User.class), eq("myKey"))).thenReturn(new IssueService.IssueResult(issue1, errorCollection));
        when(errorCollection.hasAnyErrors()).thenReturn(false);

        final ParameterDeserializer<Issue> serializer = new IssueSerializer(issueService, userManager);
        final Optional<Issue> issue = serializer.deserialize(
                ImmutableMap.<String, Object>of("issue", ImmutableMap.of("key", "myKey")), "fred");

        assertThat(issue.isPresent(), is(true));
    }
}
