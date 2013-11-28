package com.atlassian.plugin.connect.plugin.module.confluence;

import java.io.IOException;

import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.module.page.SpaceAdminTabContext;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class SpaceAdminIFrameActionTest
{
    @Mock private IFrameRenderer iFrameRenderer;
    @Mock private SpaceAdminTabContext context;
    @Mock private ConfluenceUser user;

    private SpaceAdminIFrameAction action;

    @Before
    public void setup()
    {
        action = new SpaceAdminIFrameAction();
        action.setiFrameRenderer(iFrameRenderer);
        action.provideContext(context);
        AuthenticatedUserThreadLocal.set(user);
    }

    @Test
    public void testIFrameHtml() throws IOException
    {
        when(user.getName()).thenReturn("testuser");
        when(context.getPlugin()).thenReturn(mock(Plugin.class));
        action.getIframeHtml();
        verify(iFrameRenderer).render(any(IFrameContext.class), eq("testuser"));
    }
}
