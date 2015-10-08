package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleMeta;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.webpanel.WebPanelConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WebPanelModuleProvider extends AbstractConnectCoreModuleProvider<WebPanelModuleBean>
{

    private static final WebPanelModuleMeta META = new WebPanelModuleMeta();

    private final WebPanelConnectModuleDescriptorFactory webPanelFactory;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    @Autowired
    public WebPanelModuleProvider(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator,
            WebPanelConnectModuleDescriptorFactory webPanelFactory,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry)
    {
        super(pluginRetrievalService, schemaValidator);
        this.webPanelFactory = webPanelFactory;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
    }

    @Override
    public ConnectModuleMeta<WebPanelModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<WebPanelModuleBean> modules, ConnectModuleProviderContext moduleProviderContext)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<>();
        for (WebPanelModuleBean webPanel : modules)
        {
            registerIframeRenderStrategy(webPanel, moduleProviderContext.getConnectAddonBean());
            descriptors.add(webPanelFactory.createModuleDescriptor(moduleProviderContext,
                    pluginRetrievalService.getPlugin(), webPanel));
        }
        return descriptors;
    }

    private void registerIframeRenderStrategy(WebPanelModuleBean webPanel, ConnectAddonBean descriptor)
    {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addOn(descriptor.getKey())
                .module(webPanel.getKey(descriptor))
                .genericBodyTemplate()
                .urlTemplate(webPanel.getUrl())
                .title(webPanel.getDisplayName())
                .dimensions(webPanel.getLayout().getWidth(), webPanel.getLayout().getHeight())
                .build();
        iFrameRenderStrategyRegistry.register(descriptor.getKey(), webPanel.getRawKey(), renderStrategy);
    }

}
