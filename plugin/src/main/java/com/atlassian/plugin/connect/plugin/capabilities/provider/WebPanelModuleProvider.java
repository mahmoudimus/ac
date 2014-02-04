package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.webpanel.WebPanelConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WebPanelModuleProvider implements ConnectModuleProvider<WebPanelModuleBean>
{
    private final WebPanelConnectModuleDescriptorFactory webPanelFactory;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    @Autowired
    public WebPanelModuleProvider(WebPanelConnectModuleDescriptorFactory webPanelFactory,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry)
    {
        this.webPanelFactory = webPanelFactory;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, String jsonFieldName, List<WebPanelModuleBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        for (WebPanelModuleBean bean : beans)
        {
            // register an iframe rendering strategy
            IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                    .addOn(plugin.getKey())
                    .module(bean.getKey())
                    .pageTemplate()
                    .urlTemplate(bean.getUrl())
                    .title(bean.getDisplayName())
                    .dimensions(bean.getLayout().getWidth(), bean.getLayout().getHeight())
                    .build();
            iFrameRenderStrategyRegistry.register(plugin.getKey(), bean.getKey(), renderStrategy);

            // construct a module descriptor that will supply a web panel to the product
            descriptors.addAll(beanToDescriptors(plugin, bean));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(Plugin plugin, WebPanelModuleBean bean)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        descriptors.add(webPanelFactory.createModuleDescriptor(plugin, bean));

        return descriptors;
    }
}
