package com.atlassian.plugin.connect.plugin.capabilities.descriptor.macro;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.module.DynamicContentMacro;
import com.atlassian.plugin.connect.plugin.capabilities.module.MacroModuleContextExtractor;
import com.atlassian.plugin.connect.plugin.capabilities.util.MacroEnumMapper;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.plugin.connect.plugin.capabilities.provider.DynamicContentMacroModuleProvider.CONTENT_CLASSIFIER;

@ConfluenceComponent
public class DynamicContentMacroModuleDescriptorFactory extends AbstractContentMacroModuleDescriptorFactory<DynamicContentMacroModuleBean>
{
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final MacroModuleContextExtractor macroModuleContextExtractor;

    @Autowired
    public DynamicContentMacroModuleDescriptorFactory(final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            AbsoluteAddOnUrlConverter urlConverter, MacroModuleContextExtractor macroModuleContextExtractor)
    {
        super(urlConverter);
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.macroModuleContextExtractor = macroModuleContextExtractor;
    }

    @Override
    protected DOMElement createDOMElement(ConnectAddonBean addon, DynamicContentMacroModuleBean bean)
    {
        DOMElement element = super.createDOMElement(addon, bean);

        if (null != bean.getWidth())
        {
            element.setAttribute("width", bean.getWidth());
        }
        if (null != bean.getHeight())
        {
            element.setAttribute("height", bean.getHeight());
        }

        return element;
    }

    protected ModuleFactory createModuleFactory(final ConnectAddonBean addon, final DOMElement element, final DynamicContentMacroModuleBean bean)
    {
        return new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                IFrameRenderStrategy renderStrategy = iFrameRenderStrategyRegistry.getOrThrow(addon.getKey(),
                        bean.getKey(), CONTENT_CLASSIFIER);
                DynamicContentMacro macro = new DynamicContentMacro(MacroEnumMapper.map(bean.getBodyType()),
                        MacroEnumMapper.map(bean.getOutputType()), renderStrategy, macroModuleContextExtractor);

                if (bean.hasImagePlaceholder())
                {
                    return (T) decorateWithImagePlaceHolder(addon, macro, bean.getImagePlaceholder());
                }
                return (T) macro;
            }
        };
    }

}
