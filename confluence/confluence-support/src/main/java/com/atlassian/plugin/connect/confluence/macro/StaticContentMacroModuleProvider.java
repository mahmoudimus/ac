package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.request.AbsoluteAddonUrlConverter;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleMeta;
import com.atlassian.plugin.connect.api.lifecycle.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@ConfluenceComponent
public class StaticContentMacroModuleProvider extends AbstractContentMacroModuleProvider<StaticContentMacroModuleBean> {

    private static final StaticContentMacroModuleMeta META = new StaticContentMacroModuleMeta();

    private final StaticContentMacroModuleDescriptorFactory macroModuleDescriptorFactory;

    @Autowired
    public StaticContentMacroModuleProvider(PluginRetrievalService pluginRetrievalService,
                                            ConnectJsonSchemaValidator schemaValidator,
                                            StaticContentMacroModuleDescriptorFactory macroModuleDescriptorFactory,
                                            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                            HostContainer hostContainer,
                                            AbsoluteAddonUrlConverter absoluteAddonUrlConverter,
                                            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory) {
        super(pluginRetrievalService, schemaValidator, webItemModuleDescriptorFactory, hostContainer,
                absoluteAddonUrlConverter, iFrameRenderStrategyRegistry, iFrameRenderStrategyBuilderFactory);
        this.macroModuleDescriptorFactory = macroModuleDescriptorFactory;
    }

    @Override
    public ConnectModuleMeta<StaticContentMacroModuleBean> getMeta() {
        return META;
    }

    @Override
    protected ModuleDescriptor createMacroModuleDescriptor(ConnectAddonBean addon, Plugin plugin, StaticContentMacroModuleBean macroBean) {
        return macroModuleDescriptorFactory.createModuleDescriptor(macroBean, addon, plugin);
    }
}
