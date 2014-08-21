package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.macro.StaticContentMacroModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.integration.plugins.ConnectAddonI18nManager;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class StaticContentMacroModuleProvider extends AbstractContentMacroModuleProvider<StaticContentMacroModuleBean>
{
    private final StaticContentMacroModuleDescriptorFactory macroModuleDescriptorFactory;

    @Autowired
    public StaticContentMacroModuleProvider(StaticContentMacroModuleDescriptorFactory macroModuleDescriptorFactory,
                                            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                            HostContainer hostContainer,
                                            AbsoluteAddOnUrlConverter absoluteAddOnUrlConverter,
                                            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                            ConnectAddonI18nManager connectAddonI18nManager,
                                            ApplicationProperties applicationProperties)
    {
        super(webItemModuleDescriptorFactory, hostContainer, absoluteAddOnUrlConverter, iFrameRenderStrategyRegistry, iFrameRenderStrategyBuilderFactory, connectAddonI18nManager, applicationProperties);
        this.macroModuleDescriptorFactory = macroModuleDescriptorFactory;
    }

    @Override
    protected ModuleDescriptor createMacroModuleDescriptor(ConnectModuleProviderContext moduleProviderContext,Plugin theConnectPlugin, StaticContentMacroModuleBean macroBean)
    {
        return macroModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, macroBean);
    }
}
