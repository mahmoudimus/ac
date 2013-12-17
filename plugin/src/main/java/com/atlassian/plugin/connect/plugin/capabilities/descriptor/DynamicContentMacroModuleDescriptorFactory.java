package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.util.MacroEnumMapper;
import com.atlassian.plugin.connect.plugin.integration.plugins.I18nPropertiesPluginManager;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.confluence.PageMacro;
import com.atlassian.plugin.connect.plugin.module.confluence.RemoteMacroInfo;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.util.contextparameter.RequestContextParameterFactory;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.Sets;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URISyntaxException;

@ConfluenceComponent
public class DynamicContentMacroModuleDescriptorFactory extends AbstractContentMacroModuleDescriptorFactory<DynamicContentMacroModuleBean>
{
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final IFrameRenderer iFrameRenderer;
    private final UserManager userManager;

    @Autowired
    public DynamicContentMacroModuleDescriptorFactory(
            RemotablePluginAccessorFactory remotablePluginAccessorFactory,
            IFrameRenderer iFrameRenderer,
            UserManager userManager,
            AbsoluteAddOnUrlConverter urlConverter,
            I18nPropertiesPluginManager i18nPropertiesPluginManager)
    {
        super(urlConverter, i18nPropertiesPluginManager);
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.iFrameRenderer = iFrameRenderer;
        this.userManager = userManager;
    }

    protected ModuleFactory createModuleFactory(final Plugin plugin, final DOMElement element, final DynamicContentMacroModuleBean bean)
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
                            bean.createUri(), HttpMethod.GET);

                    IFrameParams params = new IFrameParamsImpl(element);
                    IFrameContext iFrameContext = new IFrameContextImpl(
                            plugin.getKey(),
                            macroInfo.getUrl(),
                            bean.getKey(),
                            params
                    );
                    return (T) new PageMacro(macroInfo, userManager, iFrameRenderer, iFrameContext, remotablePluginAccessorFactory);
                }
                catch (URISyntaxException e)
                {
                    throw new PluginParseException(e);
                }
            }
        };
    }
}
