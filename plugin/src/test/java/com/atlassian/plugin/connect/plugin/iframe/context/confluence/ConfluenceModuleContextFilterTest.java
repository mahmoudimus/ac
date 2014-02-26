package com.atlassian.plugin.connect.plugin.iframe.context.confluence;

import com.atlassian.confluence.pages.BlogPost;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.connect.plugin.iframe.context.HashMapModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfluenceModuleContextFilterTest
{
    @Mock private PermissionManager permissionManager;
    @Mock private UserAccessor userAccessor;
    @Mock private UserManager userManager;
    @Mock private SpaceManager spaceManager;
    @Mock private PageManager pageManager;

    private ConfluenceModuleContextFilter filter;

    @Before
    public void setup()
    {
        filter = new ConfluenceModuleContextFilter(permissionManager, userAccessor, userManager, spaceManager, pageManager);

        when(userAccessor.getExistingUserByKey(any(UserKey.class))).thenReturn(mock(ConfluenceUser.class));
    }

    @Test
    public void testFilterForbiddenPage()
    {
        Page page = mock(Page.class);
        // Mockito would have mocked this to be false by default, but I wanted it to be explicit for readability.
        when(pageManager.getAbstractPage(anyLong())).thenReturn(page);
        when(permissionManager.hasPermission(any(User.class), eq(Permission.VIEW), eq(page))).thenReturn(false);

        ModuleContextParameters params = new HashMapModuleContextParameters();
        params.put("page.id", "1234");
        params.put("page.version", "1");
        params.put("page.type", "page");

        ModuleContextParameters filtered = filter.filter(params);
        assertFalse(filtered.containsKey("page.id"));
        assertFalse(filtered.containsKey("page.version"));
        assertFalse(filtered.containsKey("page.type"));
    }

    @Test
    public void testFilterAllowedPage()
    {
        Page page = mock(Page.class);
        when(pageManager.getAbstractPage(anyLong())).thenReturn(page);
        when(permissionManager.hasPermission(any(User.class), eq(Permission.VIEW), eq(page))).thenReturn(true);

        ModuleContextParameters params = new HashMapModuleContextParameters();
        params.put("page.id", "1234");
        params.put("page.version", "2");
        params.put("page.type", "blog");

        ModuleContextParameters filtered = filter.filter(params);
        assertEquals("1234", filtered.get("page.id"));
        assertEquals("2", filtered.get("page.version"));
        assertEquals("blog", filtered.get("page.type"));
    }

    @Test
    public void testFilterAllowedBlogPost()
    {
        BlogPost post = mock(BlogPost.class);
        when(pageManager.getAbstractPage(anyLong())).thenReturn(post);
        when(permissionManager.hasPermission(any(User.class), eq(Permission.VIEW), eq(post))).thenReturn(true);

        ModuleContextParameters params = new HashMapModuleContextParameters();
        params.put("page.id", "1234");
        params.put("page.version", "2");
        params.put("page.type", "blog");

        ModuleContextParameters filtered = filter.filter(params);
        assertEquals("1234", filtered.get("page.id"));
        assertEquals("2", filtered.get("page.version"));
        assertEquals("blog", filtered.get("page.type"));
    }

    @Test
    public void testNoPageIdMeansNoPageContext()
    {
        BlogPost post = mock(BlogPost.class);
        when(pageManager.getAbstractPage(anyLong())).thenReturn(post);
        when(permissionManager.hasPermission(any(User.class), eq(Permission.VIEW), eq(post))).thenReturn(true);

        ModuleContextParameters params = new HashMapModuleContextParameters();
        params.put("page.version", "2");
        params.put("page.type", "blog");

        ModuleContextParameters filtered = filter.filter(params);
        assertFalse(filtered.containsKey("page.id"));
        assertFalse(filtered.containsKey("page.version"));
        assertFalse(filtered.containsKey("page.type"));
    }

    @Test
    public void testFilterAllowedSpaceKey()
    {
        Space space = mock(Space.class);
        when(spaceManager.getSpace(anyString())).thenReturn(space);
        when(permissionManager.hasPermission(any(User.class), eq(Permission.VIEW), eq(space))).thenReturn(true);

        ModuleContextParameters params = new HashMapModuleContextParameters();
        params.put("space.key", "TEST");

        ModuleContextParameters filtered = filter.filter(params);
        assertEquals("TEST", filtered.get("space.key"));
    }

    @Test
    public void testFilterAllowedSpaceKeyAndId()
    {
        Space space = mock(Space.class);
        when(space.getId()).thenReturn(1L);
        when(spaceManager.getSpace(anyString())).thenReturn(space);
        when(permissionManager.hasPermission(any(User.class), eq(Permission.VIEW), eq(space))).thenReturn(true);

        ModuleContextParameters params = new HashMapModuleContextParameters();
        params.put("space.key", "TEST");
        params.put("space.id", "1");

        ModuleContextParameters filtered = filter.filter(params);
        assertEquals("TEST", filtered.get("space.key"));
        assertEquals("1", filtered.get("space.id"));
    }

    @Test
    public void testFilterAllowedSpaceKeyButNotSpaceId()
    {
        Space space = mock(Space.class);
        when(space.getId()).thenReturn(1L);
        when(spaceManager.getSpace(anyString())).thenReturn(space);
        when(permissionManager.hasPermission(any(User.class), eq(Permission.VIEW), eq(space))).thenReturn(true);

        ModuleContextParameters params = new HashMapModuleContextParameters();
        params.put("space.key", "TEST");
        params.put("space.id", "2");

        ModuleContextParameters filtered = filter.filter(params);
        assertEquals("TEST", filtered.get("space.key"));
        assertFalse(filtered.containsKey("space.id"));
    }

    @Test
    public void testFilterAllowedSpaceIdButNotSpaceKey()
    {
        Space fooSpace = mock(Space.class);
        when(fooSpace.getId()).thenReturn(1L);
        when(spaceManager.getSpace("FOO")).thenReturn(fooSpace);
        when(spaceManager.getSpace(1L)).thenReturn(fooSpace);
        when(permissionManager.hasPermission(any(User.class), eq(Permission.VIEW), eq(fooSpace))).thenReturn(false);

        Space barSpace = mock(Space.class); // Sounds like a good space to be.
        when(barSpace.getId()).thenReturn(2L);
        when(spaceManager.getSpace("BAR")).thenReturn(barSpace);
        when(spaceManager.getSpace(2L)).thenReturn(barSpace);
        when(permissionManager.hasPermission(any(User.class), eq(Permission.VIEW), eq(barSpace))).thenReturn(true);

        ModuleContextParameters params = new HashMapModuleContextParameters();
        params.put("space.key", "FOO");
        params.put("space.id", "2"); // bar's id

        ModuleContextParameters filtered = filter.filter(params);
        assertFalse(filtered.containsKey("space.key"));
        assertEquals("2", filtered.get("space.id"));
    }
}
