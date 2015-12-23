package com.atlassian.plugin.connect.plugin.web.panel;

import com.atlassian.plugin.connect.api.web.PluggableParametersExtractor;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
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
    private final PluggableParametersExtractor webFragmentModuleContextExtractor;

    public WebPanelConnectModuleDescriptor(HostContainer hostContainer, WebInterfaceManager webInterfaceManager,
            ModuleFactory moduleFactory, IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            PluggableParametersExtractor webFragmentModuleContextExtractor)
    {
        super(hostContainer, moduleFactory, webInterfaceManager);
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.webFragmentModuleContextExtractor = webFragmentModuleContextExtractor;
    }

    @Override
    public WebPanel getModule()
    {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyRegistry.getOrThrow(addonKeyOnly(getKey()), moduleKeyOnly(getKey()));
        return new ConnectIFrameWebPanel(renderStrategy, webFragmentModuleContextExtractor);
    }

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }

}