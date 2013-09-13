package com.atlassian.plugin.connect.plugin.module.confluence.context.extractor;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.plugin.connect.plugin.module.confluence.context.serializer.PageSerializer;
import com.atlassian.user.EntityException;
import com.atlassian.user.User;
import com.atlassian.user.UserManager;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static com.atlassian.confluence.security.Permission.VIEW;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PageContextMapParameterExtractorTest
{

    @Mock
    private PageSerializer serializer;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private UserManager userManager;

    @Mock
    private WebInterfaceContext webInterfaceContext;

    @Mock
    private User fred;

    @Test
    public void shouldBeAbsentIfResourceNotInContext()
    {
        final PageContextMapParameterExtractor parameterExtractor = new PageContextMapParameterExtractor(serializer, permissionManager, userManager);
        Map<String, Object> context = ImmutableMap.<String, Object>of(
                "key1", "value1");

        final Optional<AbstractPage> optionalPage = parameterExtractor.extract(context);
        assertThat(optionalPage.isPresent(), is(false));
    }

    @Test
    public void shouldBeAbsentIfResourceNotOfCorrectType()
    {
        final PageContextMapParameterExtractor parameterExtractor = new PageContextMapParameterExtractor(serializer, permissionManager, userManager);
        Map<String, Object> context = ImmutableMap.<String, Object>of(
                "key1", "value1",
                "page", "myPage");

        final Optional<AbstractPage> optionalPage = parameterExtractor.extract(context);
        assertThat(optionalPage.isPresent(), is(false));
    }

    @Test
    public void shouldBePresentIfResourceInContext()
    {
        final PageContextMapParameterExtractor parameterExtractor = new PageContextMapParameterExtractor(serializer, permissionManager, userManager);
        Map<String, Object> context = ImmutableMap.<String, Object>of(
                "key1", "value1",
                "page", new Page());

        final Optional<AbstractPage> optionalPage = parameterExtractor.extract(context);
        assertThat(optionalPage.isPresent(), is(true));
    }

    @Test
    public void shouldBePresentIfWebInterfaceWithPageInContext()
    {
        final PageContextMapParameterExtractor parameterExtractor = new PageContextMapParameterExtractor(serializer, permissionManager, userManager);
        Map<String, Object> context = ImmutableMap.<String, Object>of(
                "key1", "value1",
                "webInterfaceContext", webInterfaceContext);

        when(webInterfaceContext.getPage()).thenReturn(new Page());
        final Optional<AbstractPage> optionalPage = parameterExtractor.extract(context);
        assertThat(optionalPage.isPresent(), is(true));
    }

    @Test
    public void shouldBeAbsentIfWebInterfaceWithNoPageInContext()
    {
        final PageContextMapParameterExtractor parameterExtractor = new PageContextMapParameterExtractor(serializer, permissionManager, userManager);
        Map<String, Object> context = ImmutableMap.<String, Object>of(
                "key1", "value1",
                "webInterfaceContext", webInterfaceContext);

        when(webInterfaceContext.getPage()).thenReturn(null);
        final Optional<AbstractPage> optionalPage = parameterExtractor.extract(context);
        assertThat(optionalPage.isPresent(), is(false));
    }

    @Test
    public void shouldReturnTrueWhenUserHasViewPermission() throws EntityException
    {
        final PageContextMapParameterExtractor parameterExtractor = new PageContextMapParameterExtractor(serializer, permissionManager, userManager);

        Page page = new Page();

        when(userManager.getUser("fred")).thenReturn(fred);
        when(permissionManager.hasPermission(fred, VIEW, page)).thenReturn(true);

        assertThat(parameterExtractor.hasViewPermission("fred", page), is(true));
    }

    @Test
    public void shouldReturnFalseWhenUserNotFound() throws EntityException
    {
        final PageContextMapParameterExtractor parameterExtractor = new PageContextMapParameterExtractor(serializer, permissionManager, userManager);

        Page page = new Page();

        when(userManager.getUser("fred")).thenReturn(null);
        when(permissionManager.hasPermission(fred, VIEW, page)).thenReturn(true);

        assertThat(parameterExtractor.hasViewPermission("fred", page), is(false));
    }

    @Test
    public void shouldReturnFalseWhenUserDoesNotHaveViewPermission() throws EntityException
    {
        final PageContextMapParameterExtractor parameterExtractor = new PageContextMapParameterExtractor(serializer, permissionManager, userManager);

        Page page = new Page();

        when(userManager.getUser("fred")).thenReturn(fred);
        when(permissionManager.hasPermission(fred, VIEW, page)).thenReturn(false);

        assertThat(parameterExtractor.hasViewPermission("fred", page), is(false));
    }

    @Test
    public void shouldReturnFalseWhenUserManagerThrowsEntityException() throws EntityException
    {
        final PageContextMapParameterExtractor parameterExtractor = new PageContextMapParameterExtractor(serializer, permissionManager, userManager);

        Page page = new Page();

        when(userManager.getUser("fred")).thenThrow(new EntityException());
        when(permissionManager.hasPermission(fred, VIEW, page)).thenReturn(true);

        assertThat(parameterExtractor.hasViewPermission("fred", page), is(false));
    }
}
