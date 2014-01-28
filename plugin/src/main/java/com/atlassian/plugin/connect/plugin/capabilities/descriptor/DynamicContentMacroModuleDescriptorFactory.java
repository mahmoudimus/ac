package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.module.DynamicContentMacro;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.user.UserManager;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class DynamicContentMacroModuleDescriptorFactory extends AbstractContentMacroModuleDescriptorFactory<DynamicContentMacroModuleBean>
{
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final IFrameRenderer iFrameRenderer;
    private final UserManager userManager;
    private final UrlVariableSubstitutor urlVariableSubstitutor;

    @Autowired
    public DynamicContentMacroModuleDescriptorFactory(
            AbsoluteAddOnUrlConverter urlConverter,
            IFrameRenderer iFrameRenderer,
            UserManager userManager,
            RemotablePluginAccessorFactory remotablePluginAccessorFactory,
            UrlVariableSubstitutor urlVariableSubstitutor)
    {
        super(urlConverter);
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.iFrameRenderer = iFrameRenderer;
        this.userManager = userManager;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
    }

    @Override
    protected DOMElement createDOMElement(Plugin plugin, DynamicContentMacroModuleBean bean)
    {
        DOMElement element = super.createDOMElement(plugin, bean);

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

    protected ModuleFactory createModuleFactory(final Plugin plugin, final DOMElement element, final DynamicContentMacroModuleBean bean)
    {
        return new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                DynamicContentMacro macro = new DynamicContentMacro(plugin.getKey(), bean, userManager, iFrameRenderer,
                        remotablePluginAccessorFactory, urlVariableSubstitutor);

                if (bean.hasImagePlaceholder())
                {
                    return (T) decorateWithImagePlaceHolder(plugin, macro, bean.getImagePlaceholder());
                }
                return (T) macro;
            }
        };
    }

}
