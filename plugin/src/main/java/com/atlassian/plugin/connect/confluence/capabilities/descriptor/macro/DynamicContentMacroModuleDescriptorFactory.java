package com.atlassian.plugin.connect.confluence.capabilities.descriptor.macro;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.confluence.macro.DynamicContentMacro;
import com.atlassian.plugin.connect.confluence.macro.RemoteMacroRenderer;
import com.atlassian.plugin.connect.plugin.capabilities.util.MacroEnumMapper;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class DynamicContentMacroModuleDescriptorFactory extends AbstractContentMacroModuleDescriptorFactory<DynamicContentMacroModuleBean>
{
    private final RemoteMacroRenderer remoteMacroRenderer;

    @Autowired
    public DynamicContentMacroModuleDescriptorFactory(AbsoluteAddOnUrlConverter urlConverter,
                                                      RemoteMacroRenderer remoteMacroRenderer)
    {
        super(urlConverter);
        this.remoteMacroRenderer = remoteMacroRenderer;
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

                DynamicContentMacro macro = new DynamicContentMacro(
                        addon.getKey(), bean.getRawKey(),
                        MacroEnumMapper.map(bean.getBodyType()),
                        MacroEnumMapper.map(bean.getOutputType()), remoteMacroRenderer, bean.getRenderModes());

                if (bean.hasImagePlaceholder())
                {
                    return (T) decorateWithImagePlaceHolder(addon, macro, bean.getImagePlaceholder());
                }
                return (T) macro;
            }
        };
    }

}
