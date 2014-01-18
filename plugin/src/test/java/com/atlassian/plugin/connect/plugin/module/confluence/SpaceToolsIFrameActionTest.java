package com.atlassian.plugin.connect.plugin.module.confluence;

import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.module.page.SpaceToolsTabContext;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class SpaceToolsIFrameActionTest
{
    @Mock private IFrameRenderer iFrameRenderer;
    @Mock private SpaceToolsTabContext context;
    @Mock private ConfluenceUser user;
    @Mock private Space space;

    private SpaceToolsIFrameAction action;

    @Before
    public void setup()
    {
        when(context.getUrlVariableSubstitutor()).thenReturn(new UrlVariableSubstitutor());
        when(context.getPlugin()).thenReturn(mock(Plugin.class));

        action = new SpaceToolsIFrameAction();
        action.setiFrameRenderer(iFrameRenderer);
        action.provideContext(context);

        AuthenticatedUserThreadLocal.set(user);
        when(user.getName()).thenReturn("testuser");

        action.setSpace(space);
        when(space.getKey()).thenReturn("SPC");
        when(space.getId()).thenReturn(1L);
    }

    @Test
    public void testIFrameHtmlCallsRenderer() throws IOException
    {
        when(context.getUrl()).thenReturn("/test/url");
        action.getIframeHtml();

        verify(iFrameRenderer).render(any(IFrameContext.class), eq("testuser"));
    }

    @Test
    public void testIFramePath() throws Exception
    {
        when(context.getUrl()).thenReturn("/test/url");
        action.getIframeHtml();

        IFrameContext context = captureIFrameContext();

        assertEquals("/test/url", context.getIframePath());
    }

    @Test
    public void testIFramePathWithUrlSubstitution() throws Exception
    {
        when(context.getUrl()).thenReturn("/test/url?key={space.key}");
        action.getIframeHtml();
        IFrameContext context = captureIFrameContext();
        assertEquals("/test/url?key=SPC", context.getIframePath());
    }

    private IFrameContext captureIFrameContext() throws Exception
    {
        ArgumentCaptor<IFrameContext> captor = ArgumentCaptor.forClass(IFrameContext.class);
        verify(iFrameRenderer).render(captor.capture(), eq("testuser"));
        return captor.getValue();
    }
}
