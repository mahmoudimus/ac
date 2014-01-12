package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.capabilities.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.module.StaticContentMacro;
import com.atlassian.plugin.connect.plugin.integration.plugins.I18nPropertiesPluginManager;
import com.atlassian.plugin.connect.plugin.module.confluence.MacroContentManager;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.user.UserManager;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class StaticContentMacroModuleDescriptorFactory extends AbstractContentMacroModuleDescriptorFactory<StaticContentMacroModuleBean>
{
    private final MacroContentManager macroContentManager;
    private final UserManager userManager;
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final UrlVariableSubstitutor urlVariableSubstitutor;

    @Autowired
    public StaticContentMacroModuleDescriptorFactory(
            AbsoluteAddOnUrlConverter urlConverter,
            I18nPropertiesPluginManager i18nPropertiesPluginManager,
            MacroContentManager macroContentManager,
            UserManager userManager,
            RemotablePluginAccessorFactory remotablePluginAccessorFactory,
            UrlVariableSubstitutor urlVariableSubstitutor
    )
    {
        super(urlConverter, i18nPropertiesPluginManager);
        this.macroContentManager = macroContentManager;
        this.userManager = userManager;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
    }

    protected ModuleFactory createModuleFactory(final Plugin plugin, final DOMElement element, final StaticContentMacroModuleBean bean)
    {
        return new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                StaticContentMacro macro = new StaticContentMacro(plugin.getKey(), bean, userManager, macroContentManager,
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

