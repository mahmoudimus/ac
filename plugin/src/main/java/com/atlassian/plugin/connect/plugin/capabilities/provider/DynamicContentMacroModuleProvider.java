package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.macro.DynamicContentMacroModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.integration.plugins.I18nPropertiesPluginManager;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class DynamicContentMacroModuleProvider extends AbstractContentMacroModuleProvider<DynamicContentMacroModuleBean>
{
    public static final String CONTENT_CLASSIFIER = "content";

    private final DynamicContentMacroModuleDescriptorFactory dynamicContentMacroModuleDescriptorFactory;

    @Autowired
    public DynamicContentMacroModuleProvider(DynamicContentMacroModuleDescriptorFactory macroModuleDescriptorFactory,
                                             WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                             HostContainer hostContainer,
                                             AbsoluteAddOnUrlConverter absoluteAddOnUrlConverter,
                                             IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                             IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
										     I18nPropertiesPluginManager i18nPropertiesPluginManager)
    {
        super(webItemModuleDescriptorFactory, hostContainer, absoluteAddOnUrlConverter, iFrameRenderStrategyRegistry, iFrameRenderStrategyBuilderFactory, i18nPropertiesPluginManager);
        this.dynamicContentMacroModuleDescriptorFactory = macroModuleDescriptorFactory;
    }

    @Override
    protected ModuleDescriptor createMacroModuleDescriptor(ConnectAddonBean addon,Plugin theConnectPlugin, DynamicContentMacroModuleBean macroBean)
    {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addOn(addon.getKey())
                .module(macroBean.getKey(addon))
                .genericBodyTemplate(macroBean.getOutputType() == MacroOutputType.INLINE)
                .urlTemplate(macroBean.getUrl())
                .dimensions(macroBean.getWidth(), macroBean.getHeight())
                .ensureUniqueNamespace(true)
                .build();

        iFrameRenderStrategyRegistry.register(addon.getKey(), macroBean.getRawKey(), CONTENT_CLASSIFIER, renderStrategy);

        return dynamicContentMacroModuleDescriptorFactory.createModuleDescriptor(addon, theConnectPlugin, macroBean);
    }
}
