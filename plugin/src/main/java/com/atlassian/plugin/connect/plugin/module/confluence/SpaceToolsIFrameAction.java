package com.atlassian.plugin.connect.plugin.module.confluence;

import java.io.IOException;

import com.atlassian.confluence.spaces.actions.SpaceAdminAction;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.page.SpaceToolsTabContext;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;

public class SpaceToolsIFrameAction extends SpaceAdminAction
{
    private IFrameRenderer iFrameRenderer;

    private SpaceToolsTabContext context;

    public String getIframeHtml() throws IOException
    {
        IFrameParams iFrameParams = new IFrameParamsImpl();
        IFrameContext iFrameContext = new IFrameContextImpl(context.getPlugin().getKey(), context.getUrl(), context.getSpaceToolsWebItemKey(), iFrameParams);
        return iFrameRenderer.render(iFrameContext, this.getAuthenticatedUser().getName());
    }

    public SpaceToolsTabContext getSpaceTabInfo()
    {
        return this.context;
    }

    public String getSpaceAdminWebItemKey()
    {
        return this.context.getSpaceAdminWebItemKey();
    }

    public String getSpaceToolsWebItemKey()
    {
        return this.context.getSpaceToolsWebItemKey();
    }

    public void setiFrameRenderer(IFrameRenderer iFrameRenderer)
    {
        this.iFrameRenderer = iFrameRenderer;
    }

    public void provideContext(SpaceToolsTabContext context)
    {
        this.context = context;
    }
}
