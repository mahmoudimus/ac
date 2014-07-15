package com.atlassian.plugin.connect.test.plugin.iframe.webpanel;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.BlogPost;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.actions.AbstractPageAwareAction;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.webpanel.ConfluenceWebFragmentModuleContextExtractor;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class ConfluenceWebFragmentModuleContextExtractorTest
{
    @Mock
    private UserManager userManager;
    @InjectMocks
    private ConfluenceWebFragmentModuleContextExtractor extractor;

    @Test
    public void testExtractProfileFromContext()
    {
        ConfluenceUser user = mock(ConfluenceUser.class);
        UserProfile profile = mock(UserProfile.class);
        UserKey userKey = new UserKey("test-key");

        when(user.getKey()).thenReturn(userKey);
        when(userManager.getUserProfile(eq(userKey))).thenReturn(profile);
        when(profile.getUserKey()).thenReturn(userKey);
        when(profile.getUsername()).thenReturn("tpettersen");

        ModuleContextParameters params = extractor.extractParameters(ImmutableMap.<String, Object>of("targetUser", user));
        assertEquals("test-key", params.get("profileUser.key"));
        assertEquals("tpettersen", params.get("profileUser.name"));
    }

    @Test
    public void testExtractPageFromContext()
    {
        Page page = mock(Page.class);
        when(page.getId()).thenReturn(123L);
        when(page.getVersion()).thenReturn(2);
        when(page.getType()).thenReturn("page");

        ModuleContextParameters params = extractor.extractParameters(ImmutableMap.<String, Object>of("page", page));
        assertEquals("123", params.get("page.id"));
        assertEquals("2", params.get("page.version"));
        assertEquals("page", params.get("page.type"));
    }

    @Test
    public void testExtractSpaceFromContext()
    {
        Space space = mock(Space.class);
        when(space.getKey()).thenReturn("SPACE");
        when(space.getId()).thenReturn(321L);

        ModuleContextParameters params = extractor.extractParameters(ImmutableMap.<String, Object>of("space", space));
        assertEquals("321", params.get("space.id"));
        assertEquals("SPACE", params.get("space.key"));
    }

    @Test
    public void testExtractPageAndSpaceParametersFromContextWithAbstractPageAwareAction()
    {
        Space space = mock(Space.class);
        when(space.getKey()).thenReturn("SPACE");
        when(space.getId()).thenReturn(321L);

        AbstractPage page = mock(AbstractPage.class);
        when(page.getId()).thenReturn(123L);
        when(page.getVersion()).thenReturn(2);
        when(page.getType()).thenReturn("page");
        when(page.getSpace()).thenReturn(space);

        AbstractPageAwareAction pageAwareAction = mock(AbstractPageAwareAction.class);
        when(pageAwareAction.getPage()).thenReturn(page);

        ModuleContextParameters params = extractor.extractParameters(ImmutableMap.<String, Object>of("action", pageAwareAction));

        assertEquals("123", params.get("page.id"));
        assertEquals("2", params.get("page.version"));
        assertEquals("page", params.get("page.type"));
        assertEquals("321", params.get("space.id"));
        assertEquals("SPACE", params.get("space.key"));
    }

    @Test
    public void testExtractBlogPostFromContext()
    {
        BlogPost blogPost = mock(BlogPost.class);
        when(blogPost.getId()).thenReturn(123L);

        ModuleContextParameters params = extractor.extractParameters(ImmutableMap.<String, Object>of("page", blogPost));
        assertEquals("123", params.get("page.id"));
    }
}
