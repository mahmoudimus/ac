package com.atlassian.plugin.connect.plugin.module.confluence;

import java.util.Map;

import com.atlassian.plugin.connect.plugin.module.page.SpaceAdminTabContext;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.AroundInterceptor;

public class SpaceAdminTabContextInterceptor extends AroundInterceptor
{
    @Override
    protected void after(ActionInvocation actionInvocation, String s) throws Exception
    {
    }

    @Override
    protected void before(ActionInvocation actionInvocation) throws Exception
    {
        Action action = actionInvocation.getAction();
        if (action instanceof SpaceAdminIFrameAction)
        {
            Map params = actionInvocation.getProxy().getConfig().getParams();
            SpaceAdminTabContext context = (SpaceAdminTabContext) params.get("context");
            ((SpaceAdminIFrameAction) action).provideContext(context);
        }
    }
}
