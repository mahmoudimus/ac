package com.atlassian.plugin.connect.plugin.module.confluence;

import com.atlassian.confluence.spaces.actions.SpaceAdminAction;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.confluence.ConfluenceModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.confluence.ConfluenceModuleContextParametersImpl;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyUtil;
import com.atlassian.plugin.connect.plugin.module.page.SpaceToolsTabContext;

import java.io.IOException;

public class SpaceToolsIFrameAction extends SpaceAdminAction
{
    private IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private ModuleContextFilter moduleContextFilter;
    private SpaceToolsTabContext context;

    public String getIFrameHtml() throws IOException
    {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyRegistry.getOrThrow(context.getAddOnKey(), context.getModuleKey());
        ConfluenceModuleContextParameters unfilteredContext = new ConfluenceModuleContextParametersImpl();
        unfilteredContext.addSpace(this.space);
        ModuleContextParameters filteredContext = moduleContextFilter.filter(unfilteredContext);
        return IFrameRenderStrategyUtil.renderToString(filteredContext, renderStrategy);
    }

    public SpaceToolsTabContext getSpaceTabInfo()
    {
        return context;
    }

    public String getSpaceAdminWebItemKey()
    {
        return context.getSpaceAdminWebItemKey();
    }

    public String getSpaceToolsWebItemKey()
    {
        return context.getModuleKey();
    }

    public String getTitle()
    {
        return context.getDisplayName();
    }

    public void setiFrameRenderStrategyRegistry(final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry)
    {
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
    }

    public void setModuleContextFilter(final ModuleContextFilter moduleContextFilter)
    {
        this.moduleContextFilter = moduleContextFilter;
    }

    public void provideContext(SpaceToolsTabContext context)
    {
        this.context = context;
    }
}
