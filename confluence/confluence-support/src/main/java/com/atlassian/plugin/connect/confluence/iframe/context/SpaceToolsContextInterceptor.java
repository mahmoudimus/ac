package com.atlassian.plugin.connect.confluence.iframe.context;

import com.atlassian.plugin.connect.confluence.iframe.SpaceToolsIFrameAction;
import com.atlassian.plugin.connect.confluence.iframe.SpaceToolsTabContext;

import com.google.common.annotations.VisibleForTesting;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.AroundInterceptor;

import java.util.Map;

public class SpaceToolsContextInterceptor extends AroundInterceptor
{
    @Override
    protected void after(ActionInvocation actionInvocation, String s) throws Exception
    {
    }

    @Override
    @VisibleForTesting
    public void before(ActionInvocation actionInvocation) throws Exception
    {
        Action action = actionInvocation.getAction();
        if (action instanceof SpaceToolsIFrameAction)
        {
            Map params = actionInvocation.getProxy().getConfig().getParams();
            SpaceToolsTabContext context = (SpaceToolsTabContext) params.get("context");
            ((SpaceToolsIFrameAction) action).provideContext(context);
        }
    }
}
