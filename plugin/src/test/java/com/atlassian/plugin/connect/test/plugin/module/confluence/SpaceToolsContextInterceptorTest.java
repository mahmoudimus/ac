package com.atlassian.plugin.connect.test.plugin.module.confluence;

import com.atlassian.plugin.connect.confluence.iframe.context.SpaceToolsContextInterceptor;
import com.atlassian.plugin.connect.confluence.iframe.SpaceToolsIFrameAction;
import com.atlassian.plugin.connect.confluence.iframe.SpaceToolsTabContext;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.ActionProxy;
import com.opensymphony.xwork.config.entities.ActionConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

@RunWith (MockitoJUnitRunner.class)
public class SpaceToolsContextInterceptorTest
{
    @Mock private SpaceToolsIFrameAction action;
    @Mock private ActionInvocation actionInvocation;
    @Mock private ActionProxy actionProxy;
    @Mock private ActionConfig actionConfig;

    private SpaceToolsContextInterceptor interceptor;

    @Before
    public void setup()
    {
        interceptor = new SpaceToolsContextInterceptor();
        when(actionInvocation.getProxy()).thenReturn(actionProxy);
        when(actionProxy.getConfig()).thenReturn(actionConfig);
    }

    @Test
    public void testSpaceToolsActionIntercepted() throws Exception
    {
        SpaceToolsTabContext context = mock(SpaceToolsTabContext.class);
        when(actionConfig.getParams()).thenReturn(ImmutableMap.of("context", context));
        when(actionInvocation.getAction()).thenReturn(action);
        interceptor.before(actionInvocation);
        verify(action).provideContext(same(context));
    }

    @Test
    public void testNonSpaceToolsActionIgnored() throws Exception
    {
        when(actionInvocation.getAction()).thenReturn(mock(Action.class));
        interceptor.before(actionInvocation);
        verify(actionInvocation).getAction();
        verifyNoMoreInteractions(actionInvocation);
    }
}
