package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.capabilities.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.util.MacroEnumMapper;
import com.atlassian.plugin.connect.plugin.integration.plugins.I18nPropertiesPluginManager;
import com.atlassian.plugin.connect.plugin.module.confluence.MacroContentManager;
import com.atlassian.plugin.connect.plugin.module.confluence.RemoteMacroInfo;
import com.atlassian.plugin.connect.plugin.module.confluence.StorageFormatMacro;
import com.atlassian.plugin.connect.plugin.util.contextparameter.RequestContextParameterFactory;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.collect.Sets;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URISyntaxException;

@ConfluenceComponent
public class StaticContentMacroModuleDescriptorFactory extends AbstractContentMacroModuleDescriptorFactory<StaticContentMacroModuleBean>
{
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final MacroContentManager macroContentManager;

    @Autowired
    public StaticContentMacroModuleDescriptorFactory(
            RemotablePluginAccessorFactory remotablePluginAccessorFactory,
            MacroContentManager macroContentManager,
            AbsoluteAddOnUrlConverter urlConverter,
            I18nPropertiesPluginManager i18nPropertiesPluginManager)
    {
        super(urlConverter, i18nPropertiesPluginManager);
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.macroContentManager = macroContentManager;
    }

    protected ModuleFactory createModuleFactory(final Plugin plugin, final DOMElement element, final StaticContentMacroModuleBean bean)
    {
        return new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                try
                {
                    // TODO: Replace context params by URL variable substitution --> ACDEV-677
                    RequestContextParameterFactory requestContextParameterFactory = new RequestContextParameterFactory(Sets.<String>newHashSet(), Sets.<String>newHashSet());
                    RemoteMacroInfo macroInfo = new RemoteMacroInfo(element, plugin.getKey(),
                            MacroEnumMapper.map(bean.getBodyType()),
                            MacroEnumMapper.map(bean.getOutputType()),
                            requestContextParameterFactory,
                            bean.createUri(), bean.getMethod().getMethod());
                    return (T) new StorageFormatMacro(macroInfo, macroContentManager, remotablePluginAccessorFactory);
                }
                catch (URISyntaxException e)
                {
                    throw new PluginParseException(e);
                }
            }
        };
    }
}

