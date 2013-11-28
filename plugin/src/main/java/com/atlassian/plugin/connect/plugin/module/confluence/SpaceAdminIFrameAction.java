package com.atlassian.plugin.connect.plugin.module.confluence;

import java.io.IOException;

import com.atlassian.confluence.spaces.actions.SpaceAdminAction;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.page.SpaceAdminTabContext;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;

public class SpaceAdminIFrameAction extends SpaceAdminAction
{
    private IFrameRenderer iFrameRenderer;

    private SpaceAdminTabContext context;

    public String getIframeHtml() throws IOException
    {
        IFrameParams iFrameParams = new IFrameParamsImpl();
        IFrameContext iFrameContext = new IFrameContextImpl(context.getPlugin().getKey(), context.getUrl(), "", iFrameParams);
        return iFrameRenderer.render(iFrameContext, this.getAuthenticatedUser().getName());
    }

    public SpaceAdminTabContext getSpaceTabInfo()
    {
        return this.context;
    }

    public String getSelectedWebItemKey()
    {
        return this.context.getWebItemKey();
    }

    public void setiFrameRenderer(IFrameRenderer iFrameRenderer)
    {
        this.iFrameRenderer = iFrameRenderer;
    }

    public void provideContext(SpaceAdminTabContext context)
    {
        this.context = context;
    }
}
