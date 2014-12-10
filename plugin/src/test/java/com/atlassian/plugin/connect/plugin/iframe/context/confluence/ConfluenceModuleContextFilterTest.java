package com.atlassian.plugin.connect.plugin.iframe.context.confluence;

import com.atlassian.confluence.core.ContentEntityManager;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.BlogPost;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.PluginAccessor;
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

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class ConfluenceModuleContextFilterTest
{
    @Mock
    private PermissionManager permissionManager;
    @Mock
    private UserAccessor userAccessor;
    @Mock
    private UserManager userManager;
    @Mock
    private SpaceManager spaceManager;
    @Mock
    private PageManager pageManager;
    @Mock
    private ContentEntityManager contentEntityManager;

    @Mock
    private PluginAccessor pluginAccessor;

    private ConfluenceModuleContextFilter filter;

    @Before
    public void setup()
    {
        filter = new ConfluenceModuleContextFilter(pluginAccessor, permissionManager, userAccessor, userManager, spaceManager, pageManager, contentEntityManager);
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

        // page.version and page.type are not protected information
        assertTrue(filtered.containsKey("page.version"));
        assertTrue(filtered.containsKey("page.type"));
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
        when(spaceManager.getSpace(eq("TEST"))).thenReturn(space);
        when(spaceManager.getSpace(eq(1L))).thenReturn(space);
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

    @Test
    public void testFilterProfileNameAndKeyAllowed()
    {
        ConfluenceUser bob = mock(ConfluenceUser.class);
        UserKey userKey = new UserKey("babecafe");
        when(userAccessor.getExistingUserByKey(eq(userKey))).thenReturn(bob);
        when(userAccessor.getUserByName(eq("bob"))).thenReturn(bob);
        when(permissionManager.hasPermission(any(User.class), eq(Permission.VIEW), eq(bob))).thenReturn(true);

        ModuleContextParameters params = new HashMapModuleContextParameters();
        params.put("profileUser.name", "bob");
        params.put("profileUser.key", "babecafe");

        ModuleContextParameters filtered = filter.filter(params);

        assertThat(filtered, hasEntry(is("profileUser.name"), is("bob")));
        assertThat(filtered, hasEntry(is("profileUser.key"), is("babecafe")));
    }

    @Test
    public void testFilterProfileNameAllowedAndKeyForbidden()
    {
        ConfluenceUser bob = mock(ConfluenceUser.class); // bob is allowed
        ConfluenceUser eve = mock(ConfluenceUser.class); // eve is not
        UserKey userKey = new UserKey("defaced");
        when(userAccessor.getUserByName(eq("bob"))).thenReturn(bob);
        when(userAccessor.getExistingUserByKey(eq(userKey))).thenReturn(eve);
        when(permissionManager.hasPermission(any(User.class), eq(Permission.VIEW), eq(bob))).thenReturn(true);
        when(permissionManager.hasPermission(any(User.class), eq(Permission.VIEW), eq(eve))).thenReturn(false);

        ModuleContextParameters params = new HashMapModuleContextParameters();
        params.put("profileUser.name", "bob");
        params.put("profileUser.key", "defaced");

        ModuleContextParameters filtered = filter.filter(params);

        assertThat(filtered, hasEntry(is("profileUser.name"), is("bob")));
        assertThat(filtered, not(hasEntry(is("profileUser.key"), is("defaced"))));
    }

    @Test
    public void testFilterProfileNameAndKeyForbidden()
    {
        ConfluenceUser eve = mock(ConfluenceUser.class); // eve is not
        UserKey userKey = new UserKey("defaced");
        when(userAccessor.getUserByName(eq("eve"))).thenReturn(eve);
        when(userAccessor.getExistingUserByKey(eq(userKey))).thenReturn(eve);
        when(permissionManager.hasPermission(any(User.class), eq(Permission.VIEW), eq(eve))).thenReturn(false);

        ModuleContextParameters params = new HashMapModuleContextParameters();
        params.put("profileUser.name", "eve");
        params.put("profileUser.key", "defaced");

        ModuleContextParameters filtered = filter.filter(params);

        assertThat(filtered, not(hasEntry(is("profileUser.name"), is("eve"))));
        assertThat(filtered, not(hasEntry(is("profileUser.key"), is("defaced"))));

        assertTrue("Filtered context should be empty", filtered.isEmpty());
    }

    @Test
    public void testFilterForbiddenContentDoesNotContainId()
    {
        createMockContentEntity(false);
        ModuleContextParameters filtered = filter.filter(createCustomContentParams());

        assertFalse(filtered.containsKey("content.id"));
    }

    @Test
    public void testFilterForbiddenContentAllowsNonSensitiveParams()
    {
        createMockContentEntity(false);
        ModuleContextParameters filtered = filter.filter(createCustomContentParams());

        // version/type/plugin are not protected information
        assertTrue(filtered.containsKey("content.version"));
        assertTrue(filtered.containsKey("content.type"));
        assertTrue(filtered.containsKey("content.plugin"));
    }

    @Test
    public void testFilterAllowedContent()
    {
        createMockContentEntity(true);

        ModuleContextParameters filtered = filter.filter(createCustomContentParams());

        assertEquals("1234", filtered.get("content.id"));
        assertEquals("1", filtered.get("content.version"));
        assertEquals("custom", filtered.get("content.type"));
        assertEquals("plugin:foo", filtered.get("content.plugin"));
    }

    private ModuleContextParameters createCustomContentParams()
    {
        ModuleContextParameters params = new HashMapModuleContextParameters();
        params.put("content.id", "1234");
        params.put("content.version", "1");
        params.put("content.type", "custom");
        params.put("content.plugin", "plugin:foo");
        return params;
    }

    private ContentEntityObject createMockContentEntity(boolean allowed)
    {
        ContentEntityObject content = mock(ContentEntityObject.class);
        when(contentEntityManager.getById(anyLong())).thenReturn(content);
        when(permissionManager.hasPermission(any(User.class), eq(Permission.VIEW), eq(content))).thenReturn(allowed);
        return content;
    }
}
