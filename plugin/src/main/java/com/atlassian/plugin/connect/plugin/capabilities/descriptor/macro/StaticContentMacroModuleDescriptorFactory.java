package com.atlassian.plugin.connect.plugin.capabilities.descriptor.macro;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.module.macro.RemoteMacroRenderer;
import com.atlassian.plugin.connect.plugin.capabilities.module.macro.StaticContentMacro;
import com.atlassian.plugin.connect.plugin.capabilities.util.MacroEnumMapper;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class StaticContentMacroModuleDescriptorFactory extends AbstractContentMacroModuleDescriptorFactory<StaticContentMacroModuleBean>
{
    private final RemoteMacroRenderer remoteMacroRenderer;

    @Autowired
    public StaticContentMacroModuleDescriptorFactory(AbsoluteAddOnUrlConverter urlConverter,
                                                     RemoteMacroRenderer remoteMacroRenderer)
    {
        super(urlConverter);
        this.remoteMacroRenderer = remoteMacroRenderer;
    }

    protected ModuleFactory createModuleFactory(final ConnectAddonBean addon, final DOMElement element, final StaticContentMacroModuleBean bean)
    {
        return new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                StaticContentMacro macro = new StaticContentMacro(
                        addon.getKey(), bean.getRawKey(), bean.getUrl(),
                        MacroEnumMapper.map(bean.getBodyType()), MacroEnumMapper.map(bean.getOutputType()),
                        remoteMacroRenderer);

                if (bean.hasImagePlaceholder())
                {
                    return (T) decorateWithImagePlaceHolder(addon, macro, bean.getImagePlaceholder());
                }
                return (T) macro;
            }
        };
    }
}

