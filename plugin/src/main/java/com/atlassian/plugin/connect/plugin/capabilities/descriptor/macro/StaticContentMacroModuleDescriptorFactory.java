package com.atlassian.plugin.connect.plugin.capabilities.descriptor.macro;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.module.MacroModuleContextExtractor;
import com.atlassian.plugin.connect.plugin.capabilities.module.StaticContentMacro;
import com.atlassian.plugin.connect.plugin.capabilities.util.MacroEnumMapper;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.plugin.module.confluence.MacroContentManager;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class StaticContentMacroModuleDescriptorFactory extends AbstractContentMacroModuleDescriptorFactory<StaticContentMacroModuleBean>
{
    private final MacroContentManager macroContentManager;
    private final MacroModuleContextExtractor macroModuleContextExtractor;
    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;

    @Autowired
    public StaticContentMacroModuleDescriptorFactory(AbsoluteAddOnUrlConverter urlConverter,
            MacroContentManager macroContentManager, MacroModuleContextExtractor macroModuleContextExtractor,
            IFrameUriBuilderFactory iFrameUriBuilderFactory,
            RemotablePluginAccessorFactory remotablePluginAccessorFactory)
    {
        super(urlConverter);
        this.macroContentManager = macroContentManager;
        this.macroModuleContextExtractor = macroModuleContextExtractor;
        this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
    }

    protected ModuleFactory createModuleFactory(final ConnectAddonBean addon, final DOMElement element, final StaticContentMacroModuleBean bean)
    {
        return new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                StaticContentMacro macro = new StaticContentMacro(
                        addon.getKey(), bean.getKey(), bean.getUrl(),
                        MacroEnumMapper.map(bean.getBodyType()), MacroEnumMapper.map(bean.getOutputType()),
                        iFrameUriBuilderFactory, macroModuleContextExtractor, macroContentManager,
                        remotablePluginAccessorFactory);

                if (bean.hasImagePlaceholder())
                {
                    return (T) decorateWithImagePlaceHolder(addon, macro, bean.getImagePlaceholder());
                }
                return (T) macro;
            }
        };
    }
}

