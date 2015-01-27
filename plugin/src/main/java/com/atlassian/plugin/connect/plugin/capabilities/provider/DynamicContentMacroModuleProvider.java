package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.macro.DynamicContentMacroModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.integration.plugins.ConnectAddonI18nManager;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@ConfluenceComponent
public class DynamicContentMacroModuleProvider extends AbstractContentMacroModuleProvider<DynamicContentMacroModuleBean>
{
    public static final String CONTENT_CLASSIFIER = "content";

    private final DynamicContentMacroModuleDescriptorFactory dynamicContentMacroModuleDescriptorFactory;

    @Autowired
    public DynamicContentMacroModuleProvider(DynamicContentMacroModuleDescriptorFactory macroModuleDescriptorFactory,
                                             WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                             @Qualifier("hostContainer") HostContainer hostContainer,
                                             AbsoluteAddOnUrlConverter absoluteAddOnUrlConverter,
                                             IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                             IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
										     ConnectAddonI18nManager connectAddonI18nManager)
    {
        super(webItemModuleDescriptorFactory, hostContainer, absoluteAddOnUrlConverter, iFrameRenderStrategyRegistry, iFrameRenderStrategyBuilderFactory, connectAddonI18nManager);
        this.dynamicContentMacroModuleDescriptorFactory = macroModuleDescriptorFactory;
    }

    @Override
    protected ModuleDescriptor createMacroModuleDescriptor(ConnectModuleProviderContext moduleProviderContext,
                                                           Plugin theConnectPlugin, DynamicContentMacroModuleBean macroBean)
    {
        final ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addOn(connectAddonBean.getKey())
                .module(macroBean.getRawKey())
                .genericBodyTemplate(macroBean.getOutputType() == MacroOutputType.INLINE)
                .urlTemplate(macroBean.getUrl())
                .dimensions(macroBean.getWidth(), macroBean.getHeight())
                .ensureUniqueNamespace(true)
                .build();

        iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), macroBean.getRawKey(), CONTENT_CLASSIFIER, renderStrategy);

        return dynamicContentMacroModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, macroBean);
    }
}
