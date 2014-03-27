package com.atlassian.plugin.connect.plugin.capabilities.descriptor.webpanel;

import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.iframe.webpanel.ConnectIFrameWebPanel;
import com.atlassian.plugin.connect.plugin.iframe.webpanel.WebFragmentModuleContextExtractor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebPanel;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonKeyOnly;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.moduleKeyOnly;

/**
 *
 */
public class WebPanelConnectModuleDescriptor extends DefaultWebPanelModuleDescriptor
{
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final ModuleContextFilter moduleContextFilter;
    private final WebFragmentModuleContextExtractor webFragmentModuleContextExtractor;

    public WebPanelConnectModuleDescriptor(HostContainer hostContainer, WebInterfaceManager webInterfaceManager,
            ModuleFactory moduleFactory, IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            ModuleContextFilter moduleContextFilter, WebFragmentModuleContextExtractor webFragmentModuleContextExtractor)
    {
        super(hostContainer, moduleFactory, webInterfaceManager);
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.moduleContextFilter = moduleContextFilter;
        this.webFragmentModuleContextExtractor = webFragmentModuleContextExtractor;
    }

    @Override
    public WebPanel getModule()
    {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyRegistry.getOrThrow(addonKeyOnly(getKey()), moduleKeyOnly(getKey()));
        return new ConnectIFrameWebPanel(renderStrategy, moduleContextFilter, webFragmentModuleContextExtractor);
    }
}