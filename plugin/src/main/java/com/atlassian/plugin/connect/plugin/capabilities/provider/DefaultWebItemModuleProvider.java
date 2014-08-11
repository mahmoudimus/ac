package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.iframe.servlet.ConnectIFrameServlet;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.plugin.iframe.servlet.ConnectIFrameServlet.RAW_CLASSIFIER;

@Component
@ExportAsDevService
public class DefaultWebItemModuleProvider implements WebItemModuleProvider
{
    private final WebItemModuleDescriptorFactory webItemFactory;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    @Autowired
    public DefaultWebItemModuleProvider(WebItemModuleDescriptorFactory webItemFactory,
                                        IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                        IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry)
    {
        this.webItemFactory = webItemFactory;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
    }

    @Override
    public List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin,
                                                 String jsonFieldName, List<WebItemModuleBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        for (WebItemModuleBean bean : beans)
        {
            descriptors.addAll(beanToDescriptors(moduleProviderContext, theConnectPlugin, bean));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(ConnectModuleProviderContext moduleProviderContext,
                                                                     Plugin theConnectPlugin, WebItemModuleBean bean)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();
        final ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();

        if (bean.isAbsolute() || bean.getContext().equals(AddOnUrlContext.product))
        {
            descriptors.add(webItemFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, bean));
        }
        else
        {
            String localUrl = ConnectIFrameServlet.iFrameServletPath(connectAddonBean.getKey(),bean.getUrl());
            WebItemModuleBean newBean = newWebItemBean(bean).withUrl(localUrl).build();
            descriptors.add(webItemFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, newBean));
        }
        // Allow a web item which opens in a dialog to be opened programmatically, too
        if (bean.getTarget().isDialogTarget() || bean.getTarget().isInlineDialogTarget())
        {
            IFrameRenderStrategy rawRenderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                    .addOn(connectAddonBean.getKey())
                    .module(bean.getKey(connectAddonBean))
                    .genericBodyTemplate()
                    .urlTemplate(bean.getUrl())
                    .conditions(bean.getConditions())
                    .dimensions("100%", "100%") // the client (js) will size the parent of the iframe
                    .build();
            iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), bean.getKey(connectAddonBean), RAW_CLASSIFIER, rawRenderStrategy);
        }

        return descriptors;
    }
}
