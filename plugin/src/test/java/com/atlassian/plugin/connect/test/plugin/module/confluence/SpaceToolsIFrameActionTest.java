package com.atlassian.plugin.connect.test.plugin.module.confluence;

import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.connect.confluence.iframe.SpaceToolsIFrameAction;
import com.atlassian.plugin.connect.confluence.iframe.SpaceToolsTabContext;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class SpaceToolsIFrameActionTest
{
    private static final String TEST_URL = "/test/url";

    @Mock private IFrameRenderer iFrameRenderer;
    @Mock private SpaceToolsTabContext context;
    @Mock private ConfluenceUser user;
    @Mock private Space space;

    private SpaceToolsIFrameAction action;

    @Before
    public void setup()
    {
//        when(context.getUrlVariableSubstitutor()).thenReturn(new UrlVariableSubstitutor());
//        when(context.getPlugin()).thenReturn(mock(Plugin.class));
//
//        action = new SpaceToolsIFrameAction();
//        action.setiFrameRenderer(iFrameRenderer);
        action.provideContext(context);

        AuthenticatedUserThreadLocal.set(user);
        when(user.getName()).thenReturn("testuser");

        action.setSpace(space);
        when(space.getKey()).thenReturn("SPC");
        when(space.getId()).thenReturn(1L);
    }

    @Ignore
    @Test
    public void testIFrameHtmlCallsRenderer() throws IOException
    {
//        when(context.getUrl()).thenReturn(TEST_URL);
        action.getIFrameHtml();

        verify(iFrameRenderer).render(any(IFrameContext.class), eq("testuser"));
    }

    @Ignore
    @Test
    public void testIFramePath() throws Exception
    {
//        when(context.getUrl()).thenReturn(TEST_URL);
        action.getIFrameHtml();

        IFrameContext context = captureIFrameContext();

        assertEquals(TEST_URL, context.getIframePath());
    }

    @Ignore
    @Test
    public void testIFramePathWithUrlSubstitution() throws Exception
    {
//        when(context.getUrl()).thenReturn(TEST_URL + "?key={space.key}");
        action.getIFrameHtml();
        IFrameContext context = captureIFrameContext();
        assertEquals(TEST_URL + "?key=SPC", context.getIframePath());
    }

    private IFrameContext captureIFrameContext() throws Exception
    {
        ArgumentCaptor<IFrameContext> captor = ArgumentCaptor.forClass(IFrameContext.class);
        verify(iFrameRenderer).render(captor.capture(), eq("testuser"));
        return captor.getValue();
    }
}
