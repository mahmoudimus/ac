package com.atlassian.plugin.connect.core.capabilities.descriptor.webpanel;

import com.atlassian.plugin.connect.api.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.api.iframe.webpanel.PluggableParametersExtractor;
import com.atlassian.plugin.connect.core.iframe.webpanel.ConnectIFrameWebPanel;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebPanel;
import org.springframework.beans.factory.annotation.Qualifier;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonKeyOnly;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.moduleKeyOnly;

/**
 *
 */
public class WebPanelConnectModuleDescriptor extends DefaultWebPanelModuleDescriptor
{
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final ModuleContextFilter moduleContextFilter;
    private final PluggableParametersExtractor webFragmentModuleContextExtractor;

    public WebPanelConnectModuleDescriptor(@Qualifier ("hostContainer") HostContainer hostContainer, WebInterfaceManager webInterfaceManager,
            ModuleFactory moduleFactory, IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            ModuleContextFilter moduleContextFilter, PluggableParametersExtractor webFragmentModuleContextExtractor)
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

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }

}