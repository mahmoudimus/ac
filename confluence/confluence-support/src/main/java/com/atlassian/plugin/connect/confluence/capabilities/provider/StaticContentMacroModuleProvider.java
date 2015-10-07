package com.atlassian.plugin.connect.confluence.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.confluence.capabilities.descriptor.macro.StaticContentMacroModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleMeta;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.integration.plugins.ConnectAddonI18nManager;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@ConfluenceComponent
public class StaticContentMacroModuleProvider extends AbstractContentMacroModuleProvider<StaticContentMacroModuleBean>
{
    private final StaticContentMacroModuleDescriptorFactory macroModuleDescriptorFactory;

    @Autowired
    public StaticContentMacroModuleProvider(StaticContentMacroModuleDescriptorFactory macroModuleDescriptorFactory,
                                            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                            @Qualifier ("hostContainer") HostContainer hostContainer,
                                            AbsoluteAddOnUrlConverter absoluteAddOnUrlConverter,
                                            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                            ConnectAddonI18nManager connectAddonI18nManager)
    {
        super(webItemModuleDescriptorFactory, hostContainer, absoluteAddOnUrlConverter, iFrameRenderStrategyRegistry, 
                iFrameRenderStrategyBuilderFactory, connectAddonI18nManager);
        this.macroModuleDescriptorFactory = macroModuleDescriptorFactory;
    }

    @Override
    protected ModuleDescriptor createMacroModuleDescriptor(ConnectModuleProviderContext moduleProviderContext,Plugin theConnectPlugin, StaticContentMacroModuleBean macroBean)
    {
        return macroModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, macroBean);
    }

    @Override
    public String getSchemaPrefix()
    {
        return "confluence";
    }

    @Override
    public ConnectModuleMeta<StaticContentMacroModuleBean> getMeta()
    {
        return new StaticContentMacroModuleMeta();
    }
}
