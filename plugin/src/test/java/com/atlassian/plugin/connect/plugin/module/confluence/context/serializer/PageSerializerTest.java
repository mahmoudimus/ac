package com.atlassian.plugin.connect.plugin.module.confluence.context.serializer;

import com.atlassian.confluence.content.service.PageService;
import com.atlassian.confluence.content.service.page.SinglePageLocator;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.plugin.connect.plugin.module.context.MalformedRequestException;
import com.atlassian.plugin.connect.plugin.module.context.ParameterDeserializer;
import com.atlassian.plugin.connect.plugin.module.context.ResourceNotFoundException;
import com.atlassian.plugin.connect.plugin.module.permission.UnauthorisedException;
import com.atlassian.user.EntityException;
import com.atlassian.user.User;
import com.atlassian.user.UserManager;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.confluence.security.Permission.VIEW;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PageSerializerTest
{
    @Mock
    private PageService pageService;

    @Mock
    private UserManager userManager;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private User user;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private Page page1;

    @Before
    public void init()
    {
        when(permissionManager.hasPermission(user, VIEW, page1)).thenReturn(true);
    }

    @Test
    public void shouldReturnAbsentIfNoPageInParams() throws UnauthorisedException, ResourceNotFoundException
    {
        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager, permissionManager);
        final Optional<AbstractPage> page = serializer.deserialize(ImmutableMap.of("blah", new Object()), "fred");
        assertThat(page.isPresent(), is(false));
    }

    @Test
    public void shouldThrowMalformedRequestIfIssueIsNotMap() throws UnauthorisedException, ResourceNotFoundException
    {
        thrown.expect(MalformedRequestException.class);
        thrown.expectMessage("Invalid type for parameter name page");
        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager, permissionManager);
        final Optional<AbstractPage> page = serializer.deserialize(ImmutableMap.of("page", new Object()), "fred");
        assertThat(page.isPresent(), is(false));
    }

    @Test
    public void shouldThrowMalformedRequestIfNoIdOrKeyInIssue() throws ResourceNotFoundException, UnauthorisedException
    {
        thrown.expect(MalformedRequestException.class);
        thrown.expectMessage("No identifiers in request for page");

        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager, permissionManager);
        final Optional<AbstractPage> page = serializer.deserialize(
                ImmutableMap.<String, Object>of("page", ImmutableMap.of("foo", new Object())),
                "fred");
        assertThat(page.isPresent(), is(false));
    }

    @Test
    public void shouldThrowResourceNotFoundExceptionIfNoUserForUsername() throws EntityException, ResourceNotFoundException, UnauthorisedException
    {
        // null username is treated like "guest" so should not have access to resource
        thrown.expect(ResourceNotFoundException.class);
        thrown.expectMessage("No such page");

        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager, permissionManager);
        final Optional<AbstractPage> page = serializer.deserialize(
                ImmutableMap.<String, Object>of("page", ImmutableMap.of("id", 10)), "fred");

        assertThat(page.isPresent(), is(false));
        verify(userManager, times(1)).getUser("fred");
    }

    @Test
    public void shouldThrowResourceNotFoundExceptionIfNoPageForId() throws EntityException, ResourceNotFoundException, UnauthorisedException
    {
        thrown.expect(ResourceNotFoundException.class);
        thrown.expectMessage("No such page");

        when(userManager.getUser("fred")).thenReturn(user);
        when(pageService.getIdPageLocator(10l)).thenReturn(new SinglePageLocator(null));

        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager, permissionManager);
        final Optional<AbstractPage> page = serializer.deserialize(
                ImmutableMap.<String, Object>of("page", ImmutableMap.of("id", 10)), "fred");

        assertThat(page.isPresent(), is(false));
        verify(pageService, times(1)).getIdPageLocator(10l);
    }

    @Test
    public void shouldThrowResourceNotFoundExceptionWhenNoPermisssionForPage() throws EntityException, ResourceNotFoundException, UnauthorisedException
    {
        thrown.expect(ResourceNotFoundException.class);
        thrown.expectMessage("No such page");

        when(userManager.getUser("fred")).thenReturn(user);
        when(pageService.getIdPageLocator(10l)).thenReturn(new SinglePageLocator(page1));
        when(permissionManager.hasPermission(user, VIEW, page1)).thenReturn(false);

        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager, permissionManager);
        final Optional<AbstractPage> page = serializer.deserialize(
                ImmutableMap.<String, Object>of("page", ImmutableMap.of("id", 10)), "fred");

        verify(permissionManager, times(1)).hasPermission(user, VIEW, page1);
    }

    @Test
    public void shouldReturnPageWhenTheStarsAlign() throws EntityException, ResourceNotFoundException, UnauthorisedException
    {
        when(userManager.getUser("fred")).thenReturn(user);
        when(pageService.getIdPageLocator(10l)).thenReturn(new SinglePageLocator(page1));

        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager, permissionManager);
        final Optional<AbstractPage> page = serializer.deserialize(
                ImmutableMap.<String, Object>of("page", ImmutableMap.of("id", 10)), "fred");

        assertThat(page.isPresent(), is(true));
    }
}
