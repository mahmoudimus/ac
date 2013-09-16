package com.atlassian.plugin.connect.plugin.module.confluence.context.serializer;

import com.atlassian.confluence.content.service.PageService;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.plugin.connect.plugin.module.context.ParameterDeserializer;
import com.atlassian.user.UserManager;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PageSerializerTest
{
    @Mock
    private UserManager userManager;

    @Mock
    private PageService pageService;

    @Test
    public void shouldReturnAbsentIfNoPageInParams()
    {
        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager);
        final Optional<AbstractPage> page = serializer.deserialize(ImmutableMap.of("blah", new Object()), "fred");
        assertThat(page.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfPageIsNotMap()
    {
//        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager);
//        final Optional<AbstractPage> page = serializer.deserialize(ImmutableMap.of("page", new Object()), "fred");
//        assertThat(page.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfNoIdInPage()
    {
//        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager);
//        final Optional<AbstractPage> page = serializer.deserialize(
//                ImmutableMap.<String, Object>of("page", ImmutableMap.of("foo", new Object())),
//                "fred");
//        assertThat(page.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAbsentIfNoUserForUsername()
    {
//        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager);
//        final Optional<AbstractPage> page = serializer.deserialize(
//                ImmutableMap.<String, Object>of("page", ImmutableMap.of("id", 10)), "fred");
//
//        assertThat(page.isPresent(), is(false));
//        verify(userManager, times(1)).getUserByName("fred");
    }

    @Test
    public void shouldReturnAbsentIfNoPageForId()
    {
//        when(userManager.getUserByName("fred")).thenReturn(new DelegatingApplicationUser("fred", user));
//        when(pageService.getPage(any(User.class), eq(10l))).thenReturn(new PageService.PageResult(null, errorCollection));
//        when(errorCollection.hasAnyErrors()).thenReturn(true);
//
//        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager);
//        final Optional<AbstractPage> page = serializer.deserialize(
//                ImmutableMap.<String, Object>of("page", ImmutableMap.of("id", 10)), "fred");
//
//        assertThat(page.isPresent(), is(false));
//        verify(pageService, times(1)).getPage(any(User.class), eq(10l));
    }


    @Test
    public void shouldReturnPageWhenTheStarsAlign()
    {
//        when(userManager.getUserByName("fred")).thenReturn(new DelegatingApplicationUser("fred", user));
//        when(pageService.getPage(any(User.class), eq("myKey"))).thenReturn(new PageService.PageResult(page1, errorCollection));
//        when(errorCollection.hasAnyErrors()).thenReturn(false);
//
//        final ParameterDeserializer<AbstractPage> serializer = new PageSerializer(pageService, userManager);
//        final Optional<AbstractPage> page = serializer.deserialize(
//                ImmutableMap.<String, Object>of("page", ImmutableMap.of("key", "myKey")), "fred");
//
//        assertThat(page.isPresent(), is(true));
    }
}
