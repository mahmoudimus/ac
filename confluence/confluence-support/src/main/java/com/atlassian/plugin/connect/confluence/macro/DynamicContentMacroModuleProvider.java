package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.request.AbsoluteAddonUrlConverter;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleMeta;
import com.atlassian.plugin.connect.modules.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.api.lifecycle.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@ConfluenceComponent
public class DynamicContentMacroModuleProvider extends AbstractContentMacroModuleProvider<DynamicContentMacroModuleBean>
{
    public static final String CONTENT_CLASSIFIER = "content";

    private static final DynamicContentMacroModuleMeta META = new DynamicContentMacroModuleMeta();

    private final DynamicContentMacroModuleDescriptorFactory macroModuleDescriptorFactory;

    @Autowired
    public DynamicContentMacroModuleProvider(PluginRetrievalService pluginRetrievalService,
            DynamicContentMacroModuleDescriptorFactory macroModuleDescriptorFactory,
            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            HostContainer hostContainer,
            AbsoluteAddonUrlConverter absoluteAddonUrlConverter,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            ConnectJsonSchemaValidator schemaValidator)
    {
        super(pluginRetrievalService, schemaValidator, webItemModuleDescriptorFactory, hostContainer,
                absoluteAddonUrlConverter, iFrameRenderStrategyRegistry, iFrameRenderStrategyBuilderFactory);
        this.macroModuleDescriptorFactory = macroModuleDescriptorFactory;
    }

    @Override
    public ConnectModuleMeta<DynamicContentMacroModuleBean> getMeta()
    {
        return META;
    }

    @Override
    protected ModuleDescriptor createMacroModuleDescriptor(ConnectAddonBean connectAddonBean,
            Plugin plugin, DynamicContentMacroModuleBean macroBean)
    {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addon(connectAddonBean.getKey())
                .module(macroBean.getRawKey())
                .genericBodyTemplate(macroBean.getOutputType() == MacroOutputType.INLINE)
                .urlTemplate(macroBean.getUrl())
                .dimensions(macroBean.getWidth(), macroBean.getHeight())
                .ensureUniqueNamespace(true)
                .build();

        iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), macroBean.getRawKey(), CONTENT_CLASSIFIER, renderStrategy);

        return macroModuleDescriptorFactory.createModuleDescriptor(macroBean, connectAddonBean, pluginRetrievalService.getPlugin()
        );
    }
}
