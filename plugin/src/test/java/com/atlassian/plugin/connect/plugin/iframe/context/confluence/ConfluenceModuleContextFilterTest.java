package com.atlassian.plugin.connect.plugin.iframe.context.confluence;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.BlogPost;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
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

        ModuleContextParameters filtered = filter.filter(params);
        assertFalse(filtered.containsKey("page.id"));
    }

    @Test
    public void testFilterAllowedPage()
    {
        Page page = mock(Page.class);
        when(pageManager.getAbstractPage(anyLong())).thenReturn(page);
        when(permissionManager.hasPermission(any(User.class), eq(Permission.VIEW), eq(page))).thenReturn(true);

        ModuleContextParameters params = new HashMapModuleContextParameters();
        params.put("page.id", "1234");

        ModuleContextParameters filtered = filter.filter(params);
        assertTrue(filtered.containsKey("page.id"));
    }

    @Test
    public void testFilterAllowedBlogPost()
    {
        BlogPost post = mock(BlogPost.class);
        when(pageManager.getAbstractPage(anyLong())).thenReturn(post);
        when(permissionManager.hasPermission(any(User.class), eq(Permission.VIEW), eq(post))).thenReturn(true);

        ModuleContextParameters params = new HashMapModuleContextParameters();
        params.put("page.id", "1234");

        ModuleContextParameters filtered = filter.filter(params);
        assertTrue(filtered.containsKey("page.id"));
    }
}
