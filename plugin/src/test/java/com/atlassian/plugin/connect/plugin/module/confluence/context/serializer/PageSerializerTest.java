package com.atlassian.plugin.connect.plugin.module.confluence.context.serializer;

import com.atlassian.confluence.content.service.PageService;
import com.atlassian.confluence.content.service.page.SinglePageLocator;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.Page;
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
public class PageSerializerTest
{
    @Mock
    private PageService pageService;

    @Mock
    private UserManager userManager;

    @Mock
    private User user;

//    @Mock
//    private ErrorCollection errorCollection;

    @Mock
    private Page page1;

    @Test
    public void shouldReturnAbsentIfNoPageInParams() throws UnauthorisedException, ResourceNotFoundException
    {
        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager);
        final Optional<AbstractPage> page = serializer.deserialize(ImmutableMap.of("blah", new Object()), "fred");
        assertThat(page.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfPageIsNotMap() throws UnauthorisedException, ResourceNotFoundException
    {
        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager);
        final Optional<AbstractPage> page = serializer.deserialize(ImmutableMap.of("page", new Object()), "fred");
        assertThat(page.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfNoIdOrKeyInPage() throws ResourceNotFoundException, UnauthorisedException
    {
        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager);
        final Optional<AbstractPage> page = serializer.deserialize(
                ImmutableMap.<String, Object>of("page", ImmutableMap.of("foo", new Object())),
                "fred");
        assertThat(page.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfNoUserForUsername() throws EntityException, ResourceNotFoundException, UnauthorisedException
    {
        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager);
        final Optional<AbstractPage> page = serializer.deserialize(
                ImmutableMap.<String, Object>of("page", ImmutableMap.of("id", 10)), "fred");

        assertThat(page.isPresent(), is(false));
        verify(userManager, times(1)).getUser("fred");
    }

    @Test
    public void shouldReturnAbsentIfNoPageForId() throws EntityException, ResourceNotFoundException, UnauthorisedException
    {
        when(userManager.getUser("fred")).thenReturn(user);
        when(pageService.getIdPageLocator(10l)).thenReturn(new SinglePageLocator(null));

        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager);
        final Optional<AbstractPage> page = serializer.deserialize(
                ImmutableMap.<String, Object>of("page", ImmutableMap.of("id", 10)), "fred");

        assertThat(page.isPresent(), is(false));
        verify(pageService, times(1)).getIdPageLocator(10l);
    }

    @Test
    public void shouldReturnPageWhenTheStarsAlign() throws EntityException, ResourceNotFoundException, UnauthorisedException
    {
        when(userManager.getUser("fred")).thenReturn(user);
        when(pageService.getIdPageLocator(10l)).thenReturn(new SinglePageLocator(page1));

        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager);
        final Optional<AbstractPage> page = serializer.deserialize(
                ImmutableMap.<String, Object>of("page", ImmutableMap.of("id", 10)), "fred");

        assertThat(page.isPresent(), is(true));
    }
}
