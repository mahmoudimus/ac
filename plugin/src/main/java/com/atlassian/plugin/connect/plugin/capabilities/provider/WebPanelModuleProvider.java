package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.plugin.redirect.RedirectRegistry;
import com.atlassian.plugin.connect.api.Redirect.RedirectServletPath;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.webpanel.WebPanelConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;

@Component
public class WebPanelModuleProvider implements ConnectModuleProvider<WebPanelModuleBean>
{
    private final WebPanelConnectModuleDescriptorFactory webPanelFactory;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final RedirectRegistry redirectRegistry;

    @Autowired
    public WebPanelModuleProvider(WebPanelConnectModuleDescriptorFactory webPanelFactory,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            RedirectRegistry redirectRegistry)
    {
        this.webPanelFactory = webPanelFactory;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.redirectRegistry = redirectRegistry;
    }

    @Override
    public List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, String jsonFieldName, List<WebPanelModuleBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<>();

        final ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();
        for (WebPanelModuleBean bean : beans)
        {
            // register an iframe rendering strategy
            IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                    .addOn(connectAddonBean.getKey())
                    .module(bean.getKey(connectAddonBean))
                    .genericBodyTemplate()
                    .urlTemplate(bean.getUrl())
                    .title(bean.getDisplayName())
                    .dimensions(bean.getLayout().getWidth(), bean.getLayout().getHeight())
                    .redirect(true)
                    .build();
            iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), bean.getRawKey(), renderStrategy);

            RedirectRegistry.RedirectData redirectData = new RedirectRegistry.RedirectData(bean.getUrl());
            redirectRegistry.register(connectAddonBean.getKey(), bean.getRawKey(), redirectData);

            String localUrl = RedirectServletPath.forModule(connectAddonBean.getKey(), bean.getUrl());
            WebPanelModuleBean newBean = WebPanelModuleBean.newWebPanelBean(bean).withUrl(localUrl).build();

            // construct a module descriptor that will supply a web panel to the product
            descriptors.add(webPanelFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, newBean));
        }

        return descriptors;
    }
}
