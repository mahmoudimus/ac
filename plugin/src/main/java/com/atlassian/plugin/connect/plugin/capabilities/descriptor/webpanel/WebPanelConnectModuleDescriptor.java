package com.atlassian.plugin.connect.plugin.capabilities.descriptor.webpanel;

import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.iframe.webpanel.ConnectIFrameWebPanel;
import com.atlassian.plugin.connect.plugin.iframe.webpanel.WebPanelModuleContextExtractor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebPanel;

/**
 *
 */
public class WebPanelConnectModuleDescriptor extends DefaultWebPanelModuleDescriptor
{
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final ModuleContextFilter moduleContextFilter;
    private final WebPanelModuleContextExtractor webPanelModuleContextExtractor;

    public WebPanelConnectModuleDescriptor(HostContainer hostContainer, WebInterfaceManager webInterfaceManager,
            ModuleFactory moduleFactory, IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            ModuleContextFilter moduleContextFilter, WebPanelModuleContextExtractor webPanelModuleContextExtractor)
    {
        super(hostContainer, moduleFactory, webInterfaceManager);
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.moduleContextFilter = moduleContextFilter;
        this.webPanelModuleContextExtractor = webPanelModuleContextExtractor;
    }

    @Override
    public WebPanel getModule()
    {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyRegistry.getOrThrow(getPluginKey(), getKey());
        return new ConnectIFrameWebPanel(renderStrategy, moduleContextFilter, webPanelModuleContextExtractor);
    }

}